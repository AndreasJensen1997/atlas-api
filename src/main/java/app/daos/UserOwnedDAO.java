package app.daos;

import app.entities.Mention;
import app.enums.TargetType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Set;

public class UserOwnedDAO<T,I> extends AbstractDAO <T, I> {


    protected UserOwnedDAO(EntityManagerFactory emf, Class<T> entityClass) {
        super(emf, entityClass);
    }

    public List<T> getAllByUserId(I userId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.appUser.userId = :userId";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }




}
