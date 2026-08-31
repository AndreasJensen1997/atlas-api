package app.daos;

import app.entities.Chapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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
    public void delete(Chapter chapter) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.remove(chapter);
        em.getTransaction().commit();
        em.close();

    }
}
