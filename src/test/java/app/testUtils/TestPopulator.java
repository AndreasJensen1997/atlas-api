package app.testUtils;

import app.entities.AppUser;
import app.entities.Chapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

public final class TestPopulator {

    private TestPopulator() {}

    // A clean container record to hold all your seeded entities
    public record SeededData(
            AppUser user1,
            AppUser user2,
            AppUser user3,
            Chapter chapter1
    ) {}

    public static SeededData populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Clear all tables in the correct order (reverse dependency order)
            // or use RESTART IDENTITY CASCADE if supported by your SQL dialect
            try {
                em.createNativeQuery("TRUNCATE TABLE chapter, appuser RESTART IDENTITY CASCADE").executeUpdate();
            } catch (PersistenceException e) {
                // Fallback or handle if tables don't exist yet
            }

            // Create baseline data
            AppUser user1 = AppUser.builder().name("Andreas").email("andreas.jensen@outlook.dk").password("1234").build();
            AppUser user2 = AppUser.builder().name("morten").email("morten.jensen@outlook.dk").password("1234").build();
            AppUser user3 = AppUser.builder().name("theis").email("theis.jensen@outlook.dk").password("1234").build();

            em.persist(user1);
            em.persist(user2);
            em.persist(user3);

            // If Chapter requires a user or artifact, link it here
            Chapter chapter1 = Chapter.builder().title("Years in china ").build();
            em.persist(chapter1);

            em.getTransaction().commit();

            // Return everything neatly packaged
            return new SeededData(user1, user2, user3, chapter1);
        }
    }
}