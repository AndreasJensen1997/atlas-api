package app;

import app.config.HibernateConfig;
import app.daos.CourseDAO;
import app.daos.IDAO;
import app.daos.StudentDAO;
import app.daos.TeacherDAO;
import app.entities.Course;
import app.entities.CourseName;
import app.entities.Student;
import app.entities.Teacher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        StudentDAO studentDAO = new StudentDAO(emf);
        TeacherDAO teacherDAO = new TeacherDAO(emf);
        CourseDAO courseDAO = new CourseDAO(emf);



        
        emf.close();


    }


}
