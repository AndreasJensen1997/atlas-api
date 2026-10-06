package app.daos.generics;

import app.entities.Chapter;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserOwnedDAO<T, I> extends GenericDAO<T, I> {

    // ===== Constructor =====

    protected UserOwnedDAO(EntityManagerFactory emf, Class<T> entityClass) {
        super(emf, entityClass);
    }

    // ===== User Owned Operations =====

    public List<T> getAllByUserId(I userId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.user.id = :userId";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }

    public List<T> searchByTitle(String keyword, I userId) {
        try (EntityManager em = emf.createEntityManager()) {
            String fieldName = "title";
            String className = entityClass.getSimpleName();
            if (className.equals("Person") || className.equals("Place") || className.equals("ArtifactType")) {
                fieldName = "name";
            }
            String jpql = "SELECT e FROM " + className + " e WHERE LOWER(e." + fieldName + ") LIKE LOWER(:keyword) AND e.user.id = :userId";

            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            query.setParameter("keyword", "%" + keyword + "%");
            query.setParameter("userId", userId); // Pass the user ID here

            return query.getResultList();
        } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to search " + entityClass.getSimpleName() + "s: " + e.getMessage());
        }
    }

    public T findByTitleAndUserId(String title, I userId) {
        String className = null;
        try (EntityManager em = emf.createEntityManager()) {
            String fieldName = "title";
            className = entityClass.getSimpleName();
            if (className.equals("Person") || className.equals("Place") || className.equals("ArtifactType")) {
                fieldName = "name";
            }

            String jpql = "SELECT e FROM " + className + " e WHERE e." + fieldName + " = :title AND e.user.id = :userId";

            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            query.setParameter("title", title);
            query.setParameter("userId", userId);

            return query.getResultStream().findFirst().orElse(null);
        } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to find " + className + " by title and user: " + e.getMessage());
        }
    }
}
