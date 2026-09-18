package app.daos;

import app.daos.generics.GenericDAO;
import app.entities.AppUser;
import app.entities.Artifact;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class AppUserDAO extends GenericDAO<AppUser, Integer> {

    public AppUserDAO(EntityManagerFactory emf) {
        super(emf, AppUser.class); // Passes both the factory and the entity class up
    }


    public AppUser getUserByEmail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT a FROM AppUser a WHERE a.email = :email";
            TypedQuery<AppUser> query = em.createQuery(jpql, AppUser.class);
            query.setParameter("email", email);
            return query.getResultStream().findFirst().orElse(null);
        }
    }

}