package app.daos;

import app.config.HibernateTestConfig;
import app.daos.userOwned.ChapterDAO;
import app.entities.User;
import app.entities.Chapter;
import app.enums.Visibility;
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
class ChapterDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private ChapterDAO chapterDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);
        chapterDAO = new ChapterDAO(emf);
    }



    @Test
    void create() {
        User existingUser = seeded.user1();

        Chapter newChapter = Chapter.builder()
                .title("China")
                .subtitle("My two year exchange in china")
                .content("This will cover my years in china")
                .startDate(LocalDate.of(2012,1,1))
                .endDate(LocalDate.of(2014,1,1))
                .user(existingUser)
                .build();

        Chapter created = chapterDAO.create(newChapter);

        assertThat(created.getId(), notNullValue());
        Chapter fetched = chapterDAO.getById(created.getId());
        assertThat(fetched.getTitle(), is("China"));
        assertThat(fetched.getSubtitle(), is("My two year exchange in china"));
        assertThat(fetched.getContent(), is("This will cover my years in china"));
        assertThat(fetched.getVisibility(), is(Visibility.PRIVATE));
        assertThat(fetched.getStartDate(), is(LocalDate.of(2012,1,1)));
        assertThat(fetched.getEndDate(), is(LocalDate.of(2014,1,1)));
    }

    @Test
    void getById() {
        Chapter seed = seeded.chapter1();
        Chapter fetched = chapterDAO.getById(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getTitle(), is(seed.getTitle()));
    }

    @Test
    void getAll() {
        List<Chapter> all = chapterDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.chapter1(), seeded.chapter2(), seeded.chapter3()));
    }

   @Test
   void getAllChaptersByUserId(){

       List<Chapter> all = chapterDAO.getAllByUserId(seeded.user1().getId());

       assertThat(all, hasSize(1));
       assertThat(all, containsInAnyOrder(seeded.chapter1()));
       for (Chapter c : all) {
           assertThat(c.getUser().getId(), is(seeded.user1().getId()));
           System.out.println(c.getUser().getId() + "-" +  seeded.user1().getId());
       }
   }

    @Test
    void searchByName() {
        Chapter seed = seeded.chapter1();
        String keyword = seed.getTitle().substring(0, 3).toLowerCase();

        List<Chapter> results = chapterDAO.searchByTitle(keyword,seed.getUser().getId());

        assertThat(results, not(empty()));
        assertThat(results, hasItem(seed));
    }




    @Test
    void update() {
        Chapter seed = seeded.chapter2();
        User newUser = seeded.user2();

        Chapter updated = Chapter.builder()
                .id(seed.getId())
                .title("updated title")
                .subtitle("updated subtitle")
                .content("updated content")
                .startDate(LocalDate.of(2004,1,1))
                .endDate(LocalDate.of(2005,1,1))
                .user(newUser)

                .build();

        Chapter result = chapterDAO.update(updated);

        assertThat(result.getId(), is(seed.getId()));
        assertThat(result.getTitle(), is("updated title"));
        assertThat(result.getSubtitle(), is("updated subtitle"));
        assertThat(result.getContent(), is("updated content"));
        assertThat(result.getUpdatedAt(), is(LocalDate.now()));
        assertThat(result.getStartDate(), is(LocalDate.of(2004,1,1)));
        assertThat(result.getEndDate(), is(LocalDate.of(2005,1,1)));
    }

    @Test
    void delete() {
        Chapter seed = seeded.chapter1();

        boolean deleted = chapterDAO.delete(seed.getId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> chapterDAO.getById(seed.getId()));
    }

    @Test
    void create_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.create(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.getById(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.getById(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void update_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.update(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Chapter missing = Chapter.builder()
                .id(999_999)
                .title("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.update(missing));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.delete(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> chapterDAO.delete(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void searchByName_withNonExistentKeyword_returnsEmptyList() {
        List<Chapter> results = chapterDAO.searchByTitle("DoesNotExist12345",999);

        assertThat(results, is(empty()));
    }
}