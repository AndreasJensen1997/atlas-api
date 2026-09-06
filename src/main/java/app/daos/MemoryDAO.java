package app.daos;

import app.entities.Chapter;
import app.entities.Memory;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemoryDAO extends AbstractDAO <Memory, Integer> {

    public MemoryDAO(EntityManagerFactory emf) {
        super(emf, Memory.class);
    }


    public Set<Memory> getAllMemoriesByUserId(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT m FROM Memory m WHERE m.chapter.appUser.userId = :id";

            TypedQuery<Memory> query = em.createQuery(jpql, Memory.class);
            query.setParameter("id", id);

            List<Memory> list = query.getResultList();
            return new HashSet<>(list);
        } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to retrieve memory list: " + e.getMessage());
        }
    }


}
