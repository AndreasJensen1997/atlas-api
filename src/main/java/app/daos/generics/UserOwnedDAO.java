package app.daos.generics;

import app.entities.Chapter;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserOwnedDAO<T,I> extends GenericDAO<T, I> {


    protected UserOwnedDAO(EntityManagerFactory emf, Class<T> entityClass) {
        super(emf, entityClass);
    }

    public List<T> getAllByUserId(I userId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.user.userId = :userId";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }

    public List<T> searchByTitle(String keyword) {
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


    public T findByTitle(String title) {
        try (EntityManager em = emf.createEntityManager()) {
            String fieldName = "title";
            String className = entityClass.getSimpleName();
            if (className.equals("Person") || className.equals("Place")) {
                fieldName = "name";
            }

            // Dynamically build the query using entityClass.getSimpleName()
            String jpql = "SELECT e FROM " + className + " e WHERE e." + fieldName + " = :title";

            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            query.setParameter("title", title);

            return query.getResultStream().findFirst().orElse(null);
        } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to find " + entityClass.getSimpleName() + " by title: " + e.getMessage());
        }
    }





}
