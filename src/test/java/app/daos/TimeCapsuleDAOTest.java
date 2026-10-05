package app.daos;

import app.config.HibernateTestConfig;
import app.daos.userOwned.TimeCapsuleDAO;
import app.entities.*;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimeCapsuleDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private TimeCapsuleDAO timeCapsuleDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);
        timeCapsuleDAO = new TimeCapsuleDAO(emf);
    }



    @Test
    void create() {
        User existingUser = seeded.user1();

        TimeCapsule newCapsule = TimeCapsule.builder()
                .title("secret message")
                .subtitle("my first timecapsule")
                .content("Secret message for the future")
                .unlockDate(LocalDate.of(2029,1,1))
                .lockStatus(true)
                .user(existingUser)
                .build();

        TimeCapsule created = timeCapsuleDAO.create(newCapsule);
        TimeCapsule fetched = timeCapsuleDAO.getById(created.getTimeCapsuleId());

        assertThat(created.getTimeCapsuleId(), notNullValue());
        assertThat(fetched.getTitle(), is("secret message"));
        assertThat(fetched.getSubtitle(), is("my first timecapsule"));
        assertThat(fetched.getContent(), is("Secret message for the future"));
        assertThat(fetched.isLockStatus(), is(true));
        assertThat(fetched.getUser(), is(existingUser));
    }

    @Test
    void getById() {
        TimeCapsule seed = seeded.timeCapsule1();
        TimeCapsule fetched = timeCapsuleDAO.getById(seed.getTimeCapsuleId());
        assertThat(fetched.getTimeCapsuleId(), is(seed.getTimeCapsuleId()));
        assertThat(fetched.getContent(), is(seed.getContent()));
    }

    @Test
    void getAll() {
        List<TimeCapsule> all = timeCapsuleDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.timeCapsule1(), seeded.timeCapsule2(), seeded.timeCapsule3()));
    }

    @Test
    void getAllByUserId() {
        List<TimeCapsule> all = timeCapsuleDAO.getAllByUserId(seeded.user1().getUserId());

        assertThat(all, not(empty()));
        for (TimeCapsule t : all) {
            assertThat(t.getUser().getUserId(), is(seeded.user1().getUserId()));
        }
    }

    @Test
    void searchByName() {
        TimeCapsule seed = seeded.timeCapsule1();
        String keyword = seed.getTitle().substring(0, 3).toLowerCase();

        List<TimeCapsule> results = timeCapsuleDAO.searchByTitle(keyword,seed.getUser().getUserId());

        assertThat(results, not(empty()));
        assertThat(results, hasItem(seed));
    }



    @Test
    void update() {
        TimeCapsule seed = seeded.timeCapsule1();
        User newUser = seeded.user2();

        TimeCapsule updated = TimeCapsule.builder()
                .timeCapsuleId(seed.getTimeCapsuleId())
                .content("Updated capsule content")
                .unlockDate(seed.getUnlockDate())
                .lockStatus(true)
                .user(newUser)
                .build();

        TimeCapsule result = timeCapsuleDAO.update(updated);

        assertThat(result.getTimeCapsuleId(), is(seed.getTimeCapsuleId()));
        assertThat(result.getContent(), is("Updated capsule content"));
        assertThat(result.isLockStatus(), is(true));
        assertThat(result.getUser(), is(newUser));
    }

    @Test
    void delete() {
        TimeCapsule seed = seeded.timeCapsule1();

        boolean deleted = timeCapsuleDAO.delete(seed.getTimeCapsuleId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> timeCapsuleDAO.getById(seed.getTimeCapsuleId()));
    }

    @Test
    void create_withNullTimeCapsule_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.create(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.getById(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.getById(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void update_withNullTimeCapsule_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.update(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        TimeCapsule missing = TimeCapsule.builder()
                .timeCapsuleId(999_999)
                .content("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.update(missing));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.delete(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.delete(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void searchByName_withNonExistentKeyword_returnsEmptyList() {
        List<TimeCapsule> results = timeCapsuleDAO.searchByTitle("DoesNotExist12345",999);

        assertThat(results, is(empty()));
    }
}