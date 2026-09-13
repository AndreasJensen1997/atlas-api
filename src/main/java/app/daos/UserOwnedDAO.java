package app.daos;

import app.entities.Memory;
import app.entities.Mention;
import app.enums.TargetType;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
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

    public List<T> searchByName(String keyword) {
        try (EntityManager em = emf.createEntityManager()) {
            String fieldName = "title";
            String className = entityClass.getSimpleName();
            if (className.equals("Person") || className.equals("Place")) {
                fieldName = "name";
            }

            String jpql = "SELECT e FROM " + className + " e WHERE LOWER(e." + fieldName + ") LIKE LOWER(:keyword)";

            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to search " + entityClass.getSimpleName() + "s: " + e.getMessage());
        }
    }





}
