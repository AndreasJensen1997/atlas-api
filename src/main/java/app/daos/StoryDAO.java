package app.daos;

import app.entities.Memory;
import app.entities.Story;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoryDAO extends AbstractDAO<Story, Integer> {

    public StoryDAO(EntityManagerFactory emf) {
        super(emf, Story.class);
    }

    public Set<Story> getAllStoriesByUserId(int id) {
        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT s FROM Story s WHERE s.chapter.appUser.userId = :id";
            TypedQuery<Story> query = em.createQuery(jpql, Story.class);
            query.setParameter("id", id);

            List<Story> list = query.getResultList();
            return new HashSet<>(list);
        } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to retrieve story list: " + e.getMessage());
        }
    }


}
