package app.testUtils;

import app.entities.AppUser;
import app.entities.Artifact;
import app.entities.ArtifactType;
import app.entities.Chapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.time.LocalDate;

public final class TestPopulator {

    private TestPopulator() {
    }

    public record SeededData(
            AppUser user1, AppUser user2, AppUser user3,
            Chapter chapter1, Chapter chapter2, Chapter chapter3,
            Artifact artifact1, Artifact artifact2, Artifact artifact3,
            ArtifactType musicType,  ArtifactType objectType,ArtifactType vehicleType
    ) {
    }

    public static SeededData populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Clear all tables including the new ones in reverse dependency order
            try {
                em.createNativeQuery("TRUNCATE TABLE artifact, artifacttype, chapter, appuser RESTART IDENTITY CASCADE").executeUpdate();
            } catch (PersistenceException e) {
                // Fallback if tables don't exist yet
            }

            // Create baseline users
            AppUser user1 = AppUser.builder().name("Andreas").email("andreas.jensen@outlook.dk").password("1234").build();
            AppUser user2 = AppUser.builder().name("morten").email("morten.jensen@outlook.dk").password("1234").build();
            AppUser user3 = AppUser.builder().name("theis").email("theis.jensen@outlook.dk").password("1234").build();

            em.persist(user1);
            em.persist(user2);
            em.persist(user3);

            // Create chapters
            Chapter chapter1 = Chapter.builder().title("Years in china ").subtitle("My exchange years in china").appUser(user1).build();
            Chapter chapter2 = Chapter.builder().title("Early life ").subtitle("from crawling to walking").appUser(user2).build();
            Chapter chapter3 = Chapter.builder().title("University").subtitle("how i spent my 3 years at uni").appUser(user3).build();

            em.persist(chapter1);
            em.persist(chapter2);
            em.persist(chapter3);

            // Create and persist Artifact Types first (since Artifacts depend on them)
            ArtifactType musicType = ArtifactType.builder().typeName("Music").build();
            ArtifactType objectType = ArtifactType.builder().typeName("Physical Object").build();
            ArtifactType vehicleType = ArtifactType.builder().typeName("Vehicle").build();

            em.persist(musicType);
            em.persist(objectType);
            em.persist(vehicleType);

            // Create and persist Artifacts
            Artifact artifact1 = Artifact.builder().title("I forget where we were").subtitle("ben howard album").content("my favourite album").createdAt(LocalDate.of(2026,1,1)).appUser(user1).artifactType(musicType).build();
            Artifact artifact2 = Artifact.builder().title("Magnus the teddy").subtitle("childhood teddy").content("my favourite teddy as a kid").createdAt(LocalDate.of(2022,1,1)).appUser(user2).artifactType(objectType).build();
            Artifact artifact3 = Artifact.builder().title("Red bike").subtitle("My first bike").content("my mom got me this for my third birthday").createdAt(LocalDate.of(2002,1,1)).appUser(user3).artifactType(vehicleType).build();

            em.persist(artifact1);
            em.persist(artifact2);
            em.persist(artifact3);

            em.getTransaction().commit();

            return new SeededData(user1, user2, user3,
                    chapter1, chapter2, chapter3,
                    artifact1, artifact2, artifact3,
                    musicType,objectType,vehicleType);
        }
    }
}