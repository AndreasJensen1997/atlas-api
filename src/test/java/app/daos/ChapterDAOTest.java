package app.daos;

import app.config.HibernateTestConfig;
import app.entities.AppUser;
import app.entities.Chapter;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChapterDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private ChapterDAO chapterDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        seeded = TestPopulator.populate(emf);
        chapterDAO = new ChapterDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        Chapter newChapter = Chapter.builder()
                .title("China")
                .subtitle("My two year exchange in china")
                .content("This will cover my years in china")
                .startDate(LocalDate.of(2012,1,1))
                .endDate(LocalDate.of(2014,1,1))
                .build();

        Chapter created = chapterDAO.create(newChapter);

        assertThat(created.getChapterId(), notNullValue());
        Chapter fetched = chapterDAO.getById(created.getChapterId());
        assertThat(fetched.getTitle(), is("China"));
        assertThat(fetched.getSubtitle(), is("My two year exchange in china"));
        assertThat(fetched.getContent(), is("This will cover my years in china"));
        assertThat(fetched.getStartDate(), is(LocalDate.of(2012,1,1)));
        assertThat(fetched.getEndDate(), is(LocalDate.of(2014,1,1)));
    }

    @Test
    void getById() {
        AppUser seed = seeded.user1();
        AppUser fetched = appUserDAO.getById(seed.getUserId());
        assertThat(fetched.getUserId(), is(seed.getUserId()));
        assertThat(fetched.getName(), is(seed.getName()));
    }
//
//    @Test
//    void getAll() {
//        Set<AppUser> all = appUserDAO.getAll();
//        assertThat(all, hasSize(3));
//        assertThat(all, containsInAnyOrder(seeded.user1(), seeded.user2(), seeded.user3()));
//    }
//
//    @Test
//    void update() {
//        AppUser seed = seeded.user2();
//        AppUser updated = AppUser.builder()
//                .userId(seed.getUserId())
//                .name("Updated name")
//                .email(seed.getEmail())
//                .password(seed.getPassword())
//                .build();
//
//        AppUser result = appUserDAO.update(updated);
//
//        assertThat(result.getUserId(), is(seed.getUserId()));
//        assertThat(result.getName(), is("Updated name"));
//        assertThat(result.getEmail(), is(seed.getEmail()));
//        assertThat(result.getPassword(), is(seed.getPassword()));
//    }
//
//    @Test
//    void delete() {
//        AppUser seed = seeded.user1();
//
//        boolean deleted = appUserDAO.delete(seed.getUserId());
//
//        assertThat(deleted, is(true));
//        assertThrows(ApiException.class, () -> appUserDAO.getById(seed.getUserId()));
//    }
//
//    @Test
//    void create_withNullStudy_throwsApiException() {
//        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.create(null));
//        assertThat(ex.getCode(), is(400));
//    }
//
//    @Test
//    void getById_withNullId_throwsApiException() {
//        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.getById(null));
//        assertThat(ex.getCode(), is(400));
//    }
//
//    @Test
//    void getById_withMissingId_throwsApiException() {
//        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.getById(999_999));
//        assertThat(ex.getCode(), is(404));
//    }
//
//    @Test
//    void update_withNullStudy_throwsApiException() {
//        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.update(null));
//        assertThat(ex.getCode(), is(400));
//    }
//
//    @Test
//    void update_withMissingId_throwsApiException() {
//        AppUser missing = AppUser.builder()
//                .userId(999_999)
//                .name("Missing")
//                .build();
//
//        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.update(missing));
//        assertThat(ex.getCode(), is(404));
//    }
//
//    @Test
//    void delete_withNullId_throwsApiException() {
//        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.delete(null));
//        assertThat(ex.getCode(), is(400));
//    }
//
//    @Test
//    void delete_withMissingId_throwsApiException() {
//        ApiException ex = assertThrows(ApiException.class, () -> appUserDAO.delete(999_999));
//        assertThat(ex.getCode(), is(404));
//    }
}