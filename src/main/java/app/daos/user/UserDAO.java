package app.daos.user;

import app.daos.generics.GenericDAO;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

public class UserDAO extends GenericDAO<User, Integer> {

    public UserDAO(EntityManagerFactory emf) {
        super(emf, User.class); // Passes both the factory and the entity class up
    }


    public User getUserByEmail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT a FROM User a WHERE a.email = :email";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("email", email);
            return query.getResultStream().findFirst().orElse(null);
        }
    }

}