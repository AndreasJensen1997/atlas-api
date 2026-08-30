package app.daos;

import app.entities.Course;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class CourseDAO implements IDAO<Course, Integer> {
    EntityManagerFactory emf;


    @Override
    public Course create(Course course) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(course);
        em.getTransaction().commit();
        em.close();
        return course;
    }

    @Override
    public Course getById(Integer id) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Course course = em.find(Course.class, id);
        em.getTransaction().commit();
        em.close();

        return course;
    }

    @Override
    public Set<Course> getAll() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Set<Course> allCourses = (Set<Course>) em.createQuery("SELECT c FROM Course c ", Course.class)
                .getResultList();
        em.getTransaction().commit();
        em.close();

        return allCourses;
    }

    @Override
    public Course update(Course course) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.merge(course);
        em.getTransaction().commit();
        em.close();

        return null;
    }

    @Override
    public void delete(Course course) {

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.remove(course);
        em.getTransaction().commit();
        em.close();

    }
}
