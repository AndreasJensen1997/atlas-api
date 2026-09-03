package app.testUtils;

import app.entities.AppUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AppUserTestPopulator {

    private AppUserTestPopulator() {}

    public static Map<String, AppUser> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            LocalDate baseDate = LocalDate.of(2028,2,1);
            AppUser user1 = AppUser.builder().name("andreas").email("andreas.jensen@outlook.dk").password("1234").build();
            AppUser user2 = AppUser.builder().name("andreas").email("andreas.jensen@outlook.dk").password("1234").build();
            AppUser user3 = AppUser.builder().name("andreas").email("andreas.jensen@outlook.dk").password("1234").build();


            try {
                em.createNativeQuery("TRUNCATE TABLE appuser RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(user1);
                em.persist(user2);
                em.persist(user3);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, AppUser> seeded = new LinkedHashMap<>();
            seeded.put("user1", user1);
            seeded.put("user2", user2);
            seeded.put("user3", user3);
            return seeded;
        }
    }
}

