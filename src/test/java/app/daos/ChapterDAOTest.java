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
        AppUser existingUser = seeded.user1();

        Chapter newChapter = Chapter.builder()
                .title("China")
                .subtitle("My two year exchange in china")
                .content("This will cover my years in china")
                .startDate(LocalDate.of(2012,1,1))
                .endDate(LocalDate.of(2014,1,1))
                .appUser(existingUser)
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
        Chapter seed = seeded.chapter1();
        Chapter fetched = chapterDAO.getById(seed.getChapterId());
        assertThat(fetched.getChapterId(), is(seed.getChapterId()));
        assertThat(fetched.getTitle(), is(seed.getTitle()));
    }

    @Test
    void getAll() {
        Set<Chapter> all = chapterDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.chapter1(), seeded.chapter2(), seeded.chapter3()));
    }

   @Test
   void getAllChaptersByUserId(){

       Set<Chapter> all = chapterDAO.getAllChaptersByUserId(seeded.user1().getUserId());

       assertThat(all, hasSize(1));
       assertThat(all, containsInAnyOrder(seeded.chapter1()));
       for (Chapter c : all) {
           assertThat(c.getAppUser().getUserId(), is(seeded.user1().getUserId()));
           System.out.println(c.getAppUser().getUserId() + "-" +  seeded.user1().getUserId());
       }
   }

    @Test
    void update() {
        Chapter seed = seeded.chapter2();
        AppUser newUser = seeded.user2();

        Chapter updated = Chapter.builder()
                .chapterId(seed.getChapterId())
                .title("updated title")
                .subtitle("updated subtitle")
                .content("updated content")
                .startDate(LocalDate.of(2004,1,1))
                .endDate(LocalDate.of(2005,1,1))
                .appUser(newUser)

                .build();

        Chapter result = chapterDAO.update(updated);

        assertThat(result.getChapterId(), is(seed.getChapterId()));
        assertThat(result.getTitle(), is("updated title"));
        assertThat(result.getSubtitle(), is("updated subtitle"));
        assertThat(result.getContent(), is("updated content"));
        assertThat(result.getStartDate(), is(LocalDate.of(2004,1,1)));
        assertThat(result.getEndDate(), is(LocalDate.of(2005,1,1)));
    }

    @Test
    void delete() {
        Chapter seed = seeded.chapter1();

        boolean deleted = chapterDAO.delete(seed.getChapterId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> chapterDAO.getById(seed.getChapterId()));
    }

    @Test
    void create_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Chapter missing = Chapter.builder()
                .chapterId(999_999)
                .title("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}