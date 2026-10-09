package app.daos;

import app.config.HibernateTestConfig;
import app.daos.userOwned.GeminiPromptDAO;
import app.entities.GeminiPrompt;
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
class GeminiPromptDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private GeminiPromptDAO geminiPromptDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);
        geminiPromptDAO = new GeminiPromptDAO(emf);
    }

    // ==========================================
    // HAPPY PATH TESTS
    // ==========================================

    @Test
    void create() {
        User existingUser = seeded.user1();

        String generatedAiResponse = "1. Paris, France\n2. Rome, Italy\n3. Barcelona, Spain";

        GeminiPrompt newPrompt = GeminiPrompt.builder()
                .content(generatedAiResponse)
                .user(existingUser)
                .build();

        GeminiPrompt created = geminiPromptDAO.create(newPrompt);

        assertThat(created.getId(), notNullValue());
        GeminiPrompt fetched = geminiPromptDAO.getById(created.getId());
        assertThat(fetched.getContent(), is(generatedAiResponse));
        assertThat(fetched.getUser().getId(), is(existingUser.getId()));
    }

    @Test
    void getById() {
        GeminiPrompt seed = seeded.geminiPrompt1();
        GeminiPrompt fetched = geminiPromptDAO.getById(seed.getId());

        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getContent(), is(seed.getContent()));
    }

    @Test
    void getAll() {
        List<GeminiPrompt> all = geminiPromptDAO.getAll();

        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.geminiPrompt1(), seeded.geminiPrompt2(), seeded.geminiPrompt3()));
    }

    @Test
    void getAllPromptsByUserId() {
        // user1 owns geminiPrompt1 and geminiPrompt2
        List<GeminiPrompt> user1Prompts = geminiPromptDAO.getAllByUserId(seeded.user1().getId());

        assertThat(user1Prompts, hasSize(2));
        assertThat(user1Prompts, containsInAnyOrder(seeded.geminiPrompt1(), seeded.geminiPrompt2()));
        for (GeminiPrompt prompt : user1Prompts) {
            assertThat(prompt.getUser().getId(), is(seeded.user1().getId()));
        }
    }

    @Test
    void delete() {
        GeminiPrompt seed = seeded.geminiPrompt1();

        boolean deleted = geminiPromptDAO.delete(seed.getId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> geminiPromptDAO.getById(seed.getId()));
    }

    // ==========================================
    // EXCEPTION / EDGE CASE TESTS
    // ==========================================

    @Test
    void create_withNullPrompt_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> geminiPromptDAO.create(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> geminiPromptDAO.getById(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> geminiPromptDAO.getById(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> geminiPromptDAO.delete(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> geminiPromptDAO.delete(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }
}