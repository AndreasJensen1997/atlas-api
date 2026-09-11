package app.daos;

import app.entities.Mention;
import app.enums.TargetType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class MentionDAO extends AbstractDAO<Mention, Integer> {


    public MentionDAO(EntityManagerFactory emf) {
        super(emf, Mention.class);
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
