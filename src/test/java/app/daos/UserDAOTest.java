package app.daos;

import app.config.HibernateTestConfig;
import app.entities.User;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private UserDAO userDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        seeded = TestPopulator.populate(emf);
        userDAO = new UserDAO(emf);
    }



    @Test
    void create() {
        User newUser = User.builder()
                .name("Sofie")
                .email("sofie.jensen@outlook.dk")
                .password("1234")
                .build();

        User created = userDAO.create(newUser);

        assertThat(created.getUserId(), notNullValue());
        User fetched = userDAO.getById(created.getUserId());
        assertThat(fetched.getName(), is("Sofie"));
        assertThat(fetched.getEmail(), is("sofie.jensen@outlook.dk"));
    }

    @Test
    void getById() {
        User seed = seeded.user1();
        User fetched = userDAO.getById(seed.getUserId());
        assertThat(fetched.getUserId(), is(seed.getUserId()));
        assertThat(fetched.getName(), is(seed.getName()));
    }

    @Test
    void getAll() {
        List<User> all = userDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.user1(), seeded.user2(), seeded.user3()));
    }

    @Test
    void update() {
        User seed = seeded.user2();
        User updated = User.builder()
                .userId(seed.getUserId())
                .name("Updated name")
                .email(seed.getEmail())
                .password(seed.getPassword())
                .build();

        User result = userDAO.update(updated);

        assertThat(result.getUserId(), is(seed.getUserId()));
        assertThat(result.getName(), is("Updated name"));
        assertThat(result.getEmail(), is(seed.getEmail()));
        assertThat(result.getPassword(), is(seed.getPassword()));
    }

    @Test
    void delete() {
        User seed = seeded.user1();

        boolean deleted = userDAO.delete(seed.getUserId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> userDAO.getById(seed.getUserId()));
    }

    @Test
    void create_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        User missing = User.builder()
                .userId(999_999)
                .name("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> userDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}