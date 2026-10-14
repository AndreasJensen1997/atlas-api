package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.TimeCapsule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

public class TimeCapsuleDAO extends UserOwnedDAO<TimeCapsule, Integer> {

    // ===== Constructor =====

    public TimeCapsuleDAO(EntityManagerFactory emf) {
        super(emf, TimeCapsule.class);
    }


    // ===== Custom methods =====

    public List<TimeCapsule> findUnlockedAndNotNotifiedCapsules() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT t FROM TimeCapsule t JOIN FETCH t.user "
                    + "WHERE t.unlockDate <= :today AND t.unlockNotified = false";
            TypedQuery<TimeCapsule> query = em.createQuery(jpql, TimeCapsule.class);
            query.setParameter("today", LocalDate.now());
            return query.getResultList();
        }
    }
}
