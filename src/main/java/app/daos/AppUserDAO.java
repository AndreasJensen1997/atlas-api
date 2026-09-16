package app.daos;

import app.daos.generics.GenericDAO;
import app.entities.AppUser;
import jakarta.persistence.EntityManagerFactory;

public class AppUserDAO extends GenericDAO<AppUser, Integer> {

    public AppUserDAO(EntityManagerFactory emf) {
        super(emf, AppUser.class); // Passes both the factory and the entity class up
    }

    // You only write custom methods here if AppUser needs
    // something unique (like findByEmail), otherwise you are done!
}