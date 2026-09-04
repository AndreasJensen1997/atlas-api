package app.daos;

import app.entities.AppUser;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractDAO <T,I> implements IDAO <T,I> {

    protected EntityManagerFactory emf;
    protected Class<T> entityClass;


    public AbstractDAO(EntityManagerFactory emf, Class<T> entityClass) {
        this.emf = emf;
        this.entityClass = entityClass;
    }

    @Override
    public T create(T t) {
        if (t == null) {
            throw new ApiException(400, "Entity is required");
        }
        String entityName = t.getClass().getSimpleName(); // Gets "Chapter", "Artifact", etc.
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(t);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Failed to create " + entityName + ": " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return t;
    }

    @Override
    public T getById(I i) {
        String entityName = entityClass.getSimpleName(); // Safe to define here for all errors

        if (i == null) {
            throw new ApiException(400, entityName + " id is required");
        }

        try (EntityManager em = emf.createEntityManager()) {
            T t = em.find(entityClass, i); // Use entityClass instead of T.class

            if (t == null) {
                throw new ApiException(404, entityName + " not found");
            }

            return t;

        } catch (PersistenceException e) {
            throw new ApiException(500, "Get " + entityName + " failed: " + e.getMessage());
        }
    }


    @Override
    public Set<T> getAll() {
        String entityName = entityClass.getSimpleName();

        try (EntityManager em = emf.createEntityManager()) {
            // Dynamically builds: "SELECT e FROM Chapter e" or "SELECT e FROM AppUser e"
            String jpql = "SELECT e FROM " + entityName + " e";

            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            List<T> list = query.getResultList();

            return new HashSet<>(list);
        } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to retrieve " + entityName + " list: " + e.getMessage());
        }
    }


    @Override
    public T update(T t) {
        String entityName = entityClass.getSimpleName();

        if (t == null) {
            throw new ApiException(400, entityName + " is required");
        }

        // Extract the ID dynamically using JPA metadata
        Object id = emf.getPersistenceUnitUtil().getIdentifier(t);
        if (id == null) {
            throw new ApiException(400, entityName + " id is required");
        }

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                // Verify the record exists before updating
                T existing = em.find(entityClass, (I) id);
                if (existing == null) {
                    throw new ApiException(404, entityName + " not found");
                }

                T updated = em.merge(t);
                em.getTransaction().commit();
                return updated;

            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Update " + entityName + " failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }


    @Override
    public boolean delete(I id) {
        String entityName = entityClass.getSimpleName();

        if (id == null) {
            throw new ApiException(400, entityName + " id is required");
        }

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                T entityToRemove = em.find(entityClass, id);
                if (entityToRemove == null) {
                    throw new ApiException(404, entityName + " not found");
                }

                em.remove(entityToRemove);
                em.getTransaction().commit();
                return true;

            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Delete " + entityName + " failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }

}
