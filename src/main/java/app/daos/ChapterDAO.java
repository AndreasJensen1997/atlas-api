package app.daos;

import app.entities.AppUser;
import app.entities.Chapter;
import app.entities.Mention;
import app.enums.TargetType;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChapterDAO extends AbstractDAO<Chapter, Integer> {

    public ChapterDAO(EntityManagerFactory emf) {
        super(emf, Chapter.class);
    }




    // ===== CUSTOM DAO METHODS =====
    public Set<Chapter> getAllChaptersByUserId(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT c FROM Chapter c WHERE c.appUser.userId = :id";

            TypedQuery<Chapter> query = em.createQuery(jpql, Chapter.class);
            query.setParameter("id", id);

            List<Chapter> list = query.getResultList();
            return new HashSet<>(list);
        } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to retrieve Chapter list: " + e.getMessage());
        }
    }

    public List<Mention> getIncomingMentions(TargetType targetType, int targetId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Mention m WHERE m.targetType = :targetType AND m.targetId = :targetId";
            TypedQuery<Mention> query = em.createQuery(jpql, Mention.class);
            query.setParameter("targetType", targetType);
            query.setParameter("targetId", targetId);
            return query.getResultList();
        }
    }

    public List<Mention> getOutgoingMentions(TargetType ownerType, int ownerId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Mention m WHERE m.ownerType = :ownerType AND m.ownerId = :ownerId";
            TypedQuery<Mention> query = em.createQuery(jpql, Mention.class);
            query.setParameter("ownerType", ownerType);
            query.setParameter("ownerId", ownerId);
            return query.getResultList();
        }
    }






}