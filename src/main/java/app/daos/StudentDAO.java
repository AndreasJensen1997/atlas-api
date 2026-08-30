package app.daos;

import app.entities.Course;
import app.entities.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;

import java.util.Set;
@AllArgsConstructor
public class StudentDAO implements IDAO<Student, Integer> {
    EntityManagerFactory emf;


    @Override
    public Student create(Student student) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(student);
        em.getTransaction().commit();
        em.close();
        return student;
    }

    @Override
    public Student getById(Integer id) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Student student = em.find(Student.class, id);
        em.getTransaction().commit();
        em.close();
        return student;
    }

    @Override
    public Set<Student> getAll() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Set<Student> allStudents = (Set<Student>) em.createQuery("SELECT s FROM Student s ", Student.class)
                .getResultList();
        em.getTransaction().commit();
        em.close();

        return allStudents;
    }

    @Override
    public Student update(Student student) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.merge(student);
        em.getTransaction().commit();
        em.close();

        return student;
    }

    @Override
    public void delete(Student student) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.remove(student);
        em.getTransaction().commit();
        em.close();
    }
}
