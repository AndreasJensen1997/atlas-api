package app.daos;

import app.config.HibernateTestConfig;
import app.daos.userOwned.StoryDAO;
import app.entities.*;
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
class StoryDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private StoryDAO storyDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);
        storyDAO = new StoryDAO(emf);
    }

    @Test
    void create() {
        User existingUser = seeded.user1();

        Story newStory = Story.builder()
                .title("Test Story")
                .subtitle("Test Subtitle")
                .content("Test story content")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 5))
                .user(existingUser)
                .build();

        Story created = storyDAO.create(newStory);
        Story fetched = storyDAO.getById(created.getStoryId());

        assertThat(created.getStoryId(), notNullValue());
        assertThat(fetched.getTitle(), is("Test Story"));
        assertThat(fetched.getSubtitle(), is("Test Subtitle"));
        assertThat(fetched.getContent(), is("Test story content"));
        assertThat(fetched.getVisibility(), is(Visibility.PRIVATE));
        assertThat(fetched.getStartDate(), is(LocalDate.of(2026, 1, 1)));
        assertThat(fetched.getEndDate(), is(LocalDate.of(2026, 1, 5)));
    }

    @Test
    void getById() {
        Story seed = seeded.story1();
        Story fetched = storyDAO.getById(seed.getStoryId());
        assertThat(fetched.getStoryId(), is(seed.getStoryId()));
        assertThat(fetched.getTitle(), is(seed.getTitle()));
    }

    @Test
    void getAllByUserId() {
        List<Story> all = storyDAO.getAllByUserId(seeded.user1().getUserId());

        assertThat(all, not(empty()));
        for (Story s : all) {
            assertThat(s.getUser().getUserId(), is(seeded.user1().getUserId()));
        }
    }

    @Test
    void searchByName() {
        Story seed = seeded.story1();
        String keyword = seed.getTitle().substring(0, 3).toLowerCase();

        List<Story> results = storyDAO.searchByTitle(keyword,seed.getUser().getUserId());

        assertThat(results, not(empty()));
        assertThat(results, hasItem(seed));
    }


    @Test
    void getAll() {
        List<Story> all = storyDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.story1(), seeded.story2(), seeded.story3()));
    }

    @Test
    void update() {
        Story seed = seeded.story1();
        User newUser = seeded.user2();

        Story updated = Story.builder()
                .storyId(seed.getStoryId())
                .title("Updated Story Title")
                .subtitle("Updated Subtitle")
                .content("Updated content")
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 2, 10))
                .user(newUser)
                .build();

        Story result = storyDAO.update(updated);

        assertThat(result.getStoryId(), is(seed.getStoryId()));
        assertThat(result.getTitle(), is("Updated Story Title"));
        assertThat(result.getSubtitle(), is("Updated Subtitle"));
        assertThat(result.getContent(), is("Updated content"));
        assertThat(result.getUpdatedAt(), is(LocalDate.now()));
        assertThat(result.getStartDate(), is(LocalDate.of(2026, 2, 1)));
        assertThat(result.getEndDate(), is(LocalDate.of(2026, 2, 10)));
    }

    @Test
    void delete() {
        Story seed = seeded.story1();

        boolean deleted = storyDAO.delete(seed.getStoryId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> storyDAO.getById(seed.getStoryId()));
    }

    @Test
    void create_withNullStory_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> storyDAO.create(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> storyDAO.getById(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> storyDAO.getById(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void update_withNullStory_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> storyDAO.update(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Story missing = Story.builder()
                .storyId(999_999)
                .title("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> storyDAO.update(missing));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> storyDAO.delete(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> storyDAO.delete(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void searchByName_withNonExistentKeyword_returnsEmptyList() {
        List<Story> results = storyDAO.searchByTitle("DoesNotExist12345",999);

        assertThat(results, is(empty()));
    }
}