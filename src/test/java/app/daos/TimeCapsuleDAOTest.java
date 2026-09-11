package app.daos;

import app.config.HibernateTestConfig;
import app.entities.*;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Date;
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
        seeded = TestPopulator.populate(emf);
        timeCapsuleDAO = new TimeCapsuleDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        AppUser existingUser = seeded.user1();

        TimeCapsule newCapsule = TimeCapsule.builder()
                .content("Secret message for the future")
                .unlockDate(LocalDate.of(2029,1,1))
                .lockStatus(true)
                .appUser(existingUser)
                .build();

        TimeCapsule created = timeCapsuleDAO.create(newCapsule);
        TimeCapsule fetched = timeCapsuleDAO.getById(created.getTimeCapsuleId());

        assertThat(created.getTimeCapsuleId(), notNullValue());
        assertThat(fetched.getContent(), is("Secret message for the future"));
        assertThat(fetched.isLockStatus(), is(true));
        assertThat(fetched.getAppUser(), is(existingUser));
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
            assertThat(t.getAppUser().getUserId(), is(seeded.user1().getUserId()));
        }
    }

    @Test
    void update() {
        TimeCapsule seed = seeded.timeCapsule1();
        AppUser newUser = seeded.user2();

        TimeCapsule updated = TimeCapsule.builder()
                .timeCapsuleId(seed.getTimeCapsuleId())
                .content("Updated capsule content")
                .unlockDate(seed.getUnlockDate())
                .lockStatus(false)
                .appUser(newUser)
                .build();

        TimeCapsule result = timeCapsuleDAO.update(updated);

        assertThat(result.getTimeCapsuleId(), is(seed.getTimeCapsuleId()));
        assertThat(result.getContent(), is("Updated capsule content"));
        assertThat(result.isLockStatus(), is(false));
        assertThat(result.getAppUser(), is(newUser));
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
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullTimeCapsule_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        TimeCapsule missing = TimeCapsule.builder()
                .timeCapsuleId(999_999)
                .content("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> timeCapsuleDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}