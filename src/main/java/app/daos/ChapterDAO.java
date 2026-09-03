package app.daos;

import app.entities.Chapter;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class ChapterDAO implements IDAO<Chapter, Integer> {
    EntityManagerFactory emf;



    @Override
    public Chapter create(Chapter chapter) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(chapter);
        em.getTransaction().commit();
        em.close();
        return chapter;
    }

    @Override
    public Chapter getById(Integer id) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Chapter chapter = em.find(Chapter.class, id);
        em.getTransaction().commit();
        em.close();

        return chapter;
    }

    @Override
    public Set<Chapter> getAll() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Set<Chapter> allChapters = (Set<Chapter>) em.createQuery("SELECT c FROM Chapter c ", Chapter.class)
                .getResultList();
        em.getTransaction().commit();
        em.close();

        return allChapters;
    }

    @Override
    public Chapter update(Chapter chapter) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.merge(chapter);
        em.getTransaction().commit();
        em.close();

        return chapter;
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null) {
            throw new ApiException(400, "Study id is required");
        }
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try {
                Chapter studyToRemove = em.find(Chapter.class, id);
                if (studyToRemove != null) {
                    em.remove(studyToRemove);
                } else {
                    throw new ApiException(404, "Study not found");
                }
                em.getTransaction().commit();
            }  catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Delete study failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return true;
    }
}
