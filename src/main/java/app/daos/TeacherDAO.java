package app.daos;
import app.entities.Student;
import app.entities.Teacher;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;

import java.util.Set;
@AllArgsConstructor
public class TeacherDAO implements IDAO <Teacher, Integer>{
    EntityManagerFactory emf;



    @Override
    public Teacher create(Teacher teacher) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(teacher);
        em.getTransaction().commit();
        em.close();

        return teacher;
    }

    @Override
    public Teacher getById(Integer id) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Teacher teacher = em.find(Teacher.class, id);
        em.getTransaction().commit();
        em.close();

        return teacher;
    }

    @Override
    public Set<Teacher> getAll() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Set<Teacher> allTeachers = (Set<Teacher>) em.createQuery("SELECT t FROM Teacher t ", Teacher.class)
                .getResultList();
        em.getTransaction().commit();
        em.close();

        return allTeachers;
    }

    @Override
    public Teacher update(Teacher teacher) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.merge(teacher);
        em.getTransaction().commit();
        em.close();

        return teacher;
    }

    @Override
    public void delete(Teacher teacher) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.remove(teacher);
        em.getTransaction().commit();
        em.close();

    }
}
