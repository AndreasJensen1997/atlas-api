package app.daos;

import app.config.HibernateTestConfig;
import app.entities.AppUser;
import app.exceptions.ApiException;
import app.testUtils.AppUserTestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppUserDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private AppUserDAO appUserDAO;
    private Map<String, AppUser> seeded;

    @BeforeEach
    void beforeEach(){
        seeded = AppUserTestPopulator.populate(emf);
        appUserDAO = new AppUserDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        AppUser user = AppUser.builder().name("andreas").email("andreas.jensen@outlook.dk").password("1234").build();

        AppUser createdUser = appUserDAO.create(user);

        assertThat(createdUser.getUserId(), notNullValue());
        AppUser fetched = appUserDAO.getById(createdUser.getUserId());


        assertThat(fetched.getName(), is("andreas"));
        assertThat(fetched.getEmail(), is("andreas.jensen@outlook.dk"));
    }

    @Test
    void getById() {
        AppUser seed = seeded.get("user1");
        AppUser fetched = appUserDAO.getById(seed.getUserId());
        assertThat(fetched.getUserId(), is(seed.getUserId()));
        assertThat(fetched.getName(), is(seed.getName()));
    }

    @Test
    void getAll() {
        Set<AppUser> all = appUserDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.get("user1"), seeded.get("user2"), seeded.get("user3")));
    }

    @Test
    void update() {
        AppUser seed = seeded.get("user2");
        LocalDate priorUpdatedAt = LocalDate.now().minusDays(2);
        AppUser updated = AppUser.builder()
                .userId(seed.getUserId())
                .name("Updated name")
                .email(seed.getEmail())
                .password(seed.getPassword())
                .build();

        AppUser result = appUserDAO.update(updated);

        assertThat(result.getUserId(), is(seed.getUserId()));
        assertThat(result.getName(), is("Updated name"));
        assertThat(result.getEmail(), is(seed.getEmail()));
        assertThat(result.getPassword(), is(seed.getPassword()));

    }

    @Test
    void delete() {
        AppUser seed = seeded.get("user1");

        boolean deleted = appUserDAO.delete(seed.getUserId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> appUserDAO.getById(seed.getUserId()));
    }

    @Test
    void create_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        AppUser missing = AppUser.builder()
                .userId(999_999)
                .name("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}