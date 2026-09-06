package app.daos;

import app.entities.AppUser;
import app.entities.Artifact;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ArtifactDAO extends AbstractDAO<Artifact, Integer> {


    public ArtifactDAO(EntityManagerFactory emf) {
        super(emf, Artifact.class);
    }


    public List<Artifact> getArtifactsByType(int userId, int artifactTypeId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT a FROM Artifact a WHERE a.appUser.userId = :userId AND a.artifactType.artifactTypeId = :typeId";
            TypedQuery<Artifact> query = em.createQuery(jpql, Artifact.class);
            query.setParameter("userId", userId);
            query.setParameter("typeId", artifactTypeId);
            return query.getResultList();
        }
    }





}