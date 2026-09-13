package app.daos;

import app.config.HibernateTestConfig;
import app.entities.*;
import app.enums.TargetType;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MentionDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private MentionDAO mentionDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        seeded = TestPopulator.populate(emf);
        mentionDAO = new MentionDAO(emf);
    }



    @Test
    void create() {
        Mention newMention = Mention.builder()
                .targetType(TargetType.PERSON)
                .targetId(1)
                .ownerType(TargetType.MEMORY)
                .ownerId(1)
                .build();

        Mention created = mentionDAO.create(newMention);
        Mention fetched = mentionDAO.getById(created.getMentionId());

        assertThat(created.getMentionId(), notNullValue());
        assertThat(fetched.getTargetType(), is(TargetType.PERSON));
        assertThat(fetched.getTargetId(), is(1));
        assertThat(fetched.getOwnerType(), is(TargetType.MEMORY));
        assertThat(fetched.getOwnerId(), is(1));
    }

    @Test
    void getById() {
        Mention seed = seeded.mention1();
        Mention fetched = mentionDAO.getById(seed.getMentionId());
        assertThat(fetched.getMentionId(), is(seed.getMentionId()));
        assertThat(fetched.getTargetType(), is(seed.getTargetType()));
    }

    @Test
    void getAll() {
        List<Mention> all = mentionDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.mention1(), seeded.mention2(), seeded.mention3()));
    }

    @Test
    void getIncomingMentions() {
        Mention seed = seeded.mention1();
        List<Mention> incoming = mentionDAO.getIncomingMentions(seed.getTargetType(), seed.getTargetId());

        assertThat(incoming, not(empty()));
        for (Mention m : incoming) {
            assertThat(m.getTargetType(), is(seed.getTargetType()));
            assertThat(m.getTargetId(), is(seed.getTargetId()));
        }
    }

    @Test
    void getOutgoingMentions() {
        Mention seed = seeded.mention1();
        List<Mention> outgoing = mentionDAO.getOutgoingMentions(seed.getOwnerType(), seed.getOwnerId());

        assertThat(outgoing, not(empty()));
        for (Mention m : outgoing) {
            assertThat(m.getOwnerType(), is(seed.getOwnerType()));
            assertThat(m.getOwnerId(), is(seed.getOwnerId()));
        }
    }

    @Test
    void update() {
        Mention seed = seeded.mention1();

        Mention updated = Mention.builder()
                .mentionId(seed.getMentionId())
                .targetType(TargetType.PLACE)
                .targetId(99)
                .ownerType(TargetType.STORY)
                .ownerId(88)
                .build();

        Mention result = mentionDAO.update(updated);

        assertThat(result.getMentionId(), is(seed.getMentionId()));
        assertThat(result.getTargetType(), is(TargetType.PLACE));
        assertThat(result.getTargetId(), is(99));
        assertThat(result.getOwnerType(), is(TargetType.STORY));
        assertThat(result.getOwnerId(), is(88));
    }

    @Test
    void delete() {
        Mention seed = seeded.mention1();

        boolean deleted = mentionDAO.delete(seed.getMentionId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> mentionDAO.getById(seed.getMentionId()));
    }

    @Test
    void create_withNullMention_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> mentionDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> mentionDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> mentionDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullMention_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> mentionDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Mention missing = Mention.builder()
                .mentionId(999_999)
                .targetId(1)
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> mentionDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> mentionDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> mentionDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}