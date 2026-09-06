package app.daos;

import app.config.HibernateTestConfig;
import app.entities.AppUser;
import app.entities.Artifact;
import app.entities.ArtifactType;
import app.entities.Chapter;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
        seeded = TestPopulator.populate(emf);
        artifactDAO = new ArtifactDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        AppUser existingUser = seeded.user1();
        ArtifactType existingArtifactType = seeded.artifact1().getArtifactType();

        Artifact newArtifact = Artifact.builder()
                .title("I forget where we were")
                .subtitle("ben howard album")
                .content("my favourite album of all time")
                .createdAt(LocalDate.of(2012, 1, 1))
                .appUser(existingUser)
                .artifactType(existingArtifactType)
                .build();

        Artifact created = artifactDAO.create(newArtifact);

        assertThat(created.getArtifactId(), notNullValue());
        Artifact fetched = artifactDAO.getById(created.getArtifactId());

        assertThat(fetched.getTitle(), is("I forget where we were"));
        assertThat(fetched.getSubtitle(), is("ben howard album"));
        assertThat(fetched.getContent(), is("my favourite album of all time"));
        assertThat(fetched.getCreatedAt(), is(LocalDate.of(2012, 1, 1)));
        assertThat(fetched.getAppUser(), is(existingUser));
        assertThat(fetched.getArtifactType().getArtifactTypeId(), is(existingArtifactType.getArtifactTypeId()));
    }

    @Test
    void getById() {
        Artifact seed = seeded.artifact1();
        Artifact fetched = artifactDAO.getById(seed.getArtifactId());
        assertThat(fetched.getArtifactId(), is(seed.getArtifactId()));
        assertThat(fetched.getTitle(), is(seed.getTitle()));
    }

    @Test
    void getAll() {
        List<Artifact> all = artifactDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.artifact1(), seeded.artifact2(), seeded.artifact3()));
    }

    @Test
    void getAllChaptersByUserId(){

        List<Artifact> all = artifactDAO.getAllByUserId(seeded.artifact1().getAppUser().getUserId());

        assertThat(all, hasSize(1));
        assertThat(all, containsInAnyOrder(seeded.artifact1()));
        for (Artifact a : all) {
            assertThat(a.getAppUser().getUserId(), is(seeded.artifact1().getAppUser().getUserId()));
            System.out.println(a.getAppUser().getUserId() + "-" +  seeded.artifact1().getAppUser().getUserId());
        }
    }

    @Test
    void update() {
        Artifact seed = seeded.artifact2();
        AppUser newUser = seeded.user2();

        Artifact updated = Artifact.builder()
                .artifactId(seed.getArtifactId())
                .title("updated title")
                .subtitle("updated subtitle")
                .content("updated content")
                .createdAt(LocalDate.of(2002,1,1))
                .artifactType(seeded.musicType())
                .appUser(newUser)

                .build();

        Artifact result = artifactDAO.update(updated);

        assertThat(result.getArtifactId(), is(seed.getArtifactId()));
        assertThat(result.getTitle(), is("updated title"));
        assertThat(result.getSubtitle(), is("updated subtitle"));
        assertThat(result.getContent(), is("updated content"));
        assertThat(result.getCreatedAt(), is(LocalDate.of(2002,1,1)));
        assertThat(result.getArtifactType(), is(seeded.musicType()));
    }

    @Test
    void delete() {
        Artifact seed = seeded.artifact1();

        boolean deleted = artifactDAO.delete(seed.getArtifactId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> artifactDAO.getById(seed.getArtifactId()));
    }

    @Test
    void create_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Artifact missing = Artifact.builder()
                .artifactId(999_999)
                .title("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () ->  artifactDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> artifactDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}