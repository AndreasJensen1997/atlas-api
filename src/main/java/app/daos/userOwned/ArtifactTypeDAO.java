package app.daos.userOwned;

import app.daos.generics.GenericDAO;
import app.daos.generics.UserOwnedDAO;
import app.entities.ArtifactType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class ArtifactTypeDAO extends UserOwnedDAO<ArtifactType, Integer> {

    // ===== Constructor =====

    public ArtifactTypeDAO(EntityManagerFactory emf) {
        super(emf,ArtifactType.class);
    }

    public ArtifactType findByNameIgnoreCase(String name, Integer userId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT t FROM ArtifactType t WHERE LOWER(t.name) = LOWER(:name) AND t.user.id = :userId",
                            ArtifactType.class)
                    .setParameter("name", name)
                    .setParameter("userId", userId)
                    .getResultStream().findFirst().orElse(null);
        }
    }

}