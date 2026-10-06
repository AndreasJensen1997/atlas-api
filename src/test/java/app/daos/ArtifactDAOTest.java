package app.daos;

import app.config.HibernateTestConfig;
import app.daos.userOwned.ArtifactDAO;
import app.entities.*;
import app.enums.Visibility;import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtifactDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private ArtifactDAO artifactDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);
        artifactDAO = new ArtifactDAO(emf);
    }



    @Test
    void create() {
        User existingUser = seeded.user1();
        ArtifactType existingArtifactType = seeded.artifact1().getArtifactType();

        Artifact newArtifact = Artifact.builder()
                .title("I forget where we were")
                .subtitle("ben howard album")
                .content("my favourite album of all time")
                .createdAt(LocalDate.of(2012, 1, 1))
                .user(existingUser)
                .artifactType(existingArtifactType)
                .build();

        Artifact created = artifactDAO.create(newArtifact);

        assertThat(created.getId(), notNullValue());
        Artifact fetched = artifactDAO.getById(created.getId());

        assertThat(fetched.getTitle(), is("I forget where we were"));
        assertThat(fetched.getSubtitle(), is("ben howard album"));
        assertThat(fetched.getContent(), is("my favourite album of all time"));
        assertThat(fetched.getCreatedAt(), is(LocalDate.of(2012, 1, 1)));
        assertThat(fetched.getVisibility(), is(Visibility.PRIVATE));
        assertThat(fetched.getUser(), is(existingUser));
        assertThat(fetched.getArtifactType().getId(), is(existingArtifactType.getId()));
    }

    @Test
    void getById() {
        Artifact seed = seeded.artifact1();
        Artifact fetched = artifactDAO.getById(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getTitle(), is(seed.getTitle()));
    }

    @Test
    void getAll() {
        List<Artifact> all = artifactDAO.getAll();
        assertThat(all, hasSize(4));
        assertThat(all, containsInAnyOrder(seeded.artifact1(), seeded.artifact2(), seeded.artifact3(),seeded.artifact4()));
    }

    @Test
    void getAllChaptersByUserId(){

        List<Artifact> all = artifactDAO.getAllByUserId(seeded.artifact1().getUser().getId());

        assertThat(all, hasSize(1));
        assertThat(all, containsInAnyOrder(seeded.artifact1()));
        for (Artifact a : all) {
            assertThat(a.getUser().getId(), is(seeded.artifact1().getUser().getId()));
            System.out.println(a.getUser().getId() + "-" +  seeded.artifact1().getUser().getId());
        }
    }

    @Test
    void searchByName() {
        Artifact seed = seeded.artifact1();
        String keyword = seed.getTitle().substring(0, 3).toLowerCase();

        List<Artifact> results = artifactDAO.searchByTitle(keyword, seed.getUser().getId());

        assertThat(results, not(empty()));
        assertThat(results, hasItem(seed));
    }

    @Test
    void getArtifactsByType() {
        Artifact seed = seeded.artifact1();
        Integer userId = seed.getUser().getId();
        Integer typeId = seed.getArtifactType().getId();

        List<Artifact> results = artifactDAO.getArtifactsByType(userId, typeId);

        assertThat(results, hasSize(1));
        assertThat(results, hasItem(seed));
    }

    @Test
    void update() {
        Artifact seed = seeded.artifact2();
        User newUser = seeded.user2();

        Artifact updated = Artifact.builder()
                .id(seed.getId())
                .title("updated title")
                .subtitle("updated subtitle")
                .content("updated content")
                .createdAt(LocalDate.of(2002,1,1))
                .artifactType(seeded.musicType())
                .user(newUser)

                .build();

        Artifact result = artifactDAO.update(updated);

        assertThat(result.getId(), is(seed.getId()));
        assertThat(result.getTitle(), is("updated title"));
        assertThat(result.getSubtitle(), is("updated subtitle"));
        assertThat(result.getContent(), is("updated content"));
        assertThat(result.getUpdatedAt(), is(LocalDate.now()));
        assertThat(result.getCreatedAt(), is(LocalDate.of(2002,1,1)));
        assertThat(result.getArtifactType(), is(seeded.musicType()));
    }

    @Test
    void delete() {
        Artifact seed = seeded.artifact1();

        boolean deleted = artifactDAO.delete(seed.getId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> artifactDAO.getById(seed.getId()));
    }

    @Test
    void create_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.create(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.getById(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.getById(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void update_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.update(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Artifact missing = Artifact.builder()
                .id(999_999)
                .title("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () ->  artifactDAO.update(missing));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.delete(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.delete(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void getArtifactsByType_withNonExistentType_returnsEmptyList() {
        Artifact seed = seeded.artifact1();
        Integer userId = seed.getUser().getId();

        List<Artifact> results = artifactDAO.getArtifactsByType(userId, 999_999);

        assertThat(results, is(empty()));
    }

    @Test
    void searchByName_withNonExistentKeyword_returnsEmptyList() {
        List<Artifact> results = artifactDAO.searchByTitle("DoesNotExist12345",999);

        assertThat(results, is(empty()));
    }
}