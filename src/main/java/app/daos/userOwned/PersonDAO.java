package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Person;
import jakarta.persistence.EntityManagerFactory;

public class PersonDAO extends UserOwnedDAO<Person, Integer> {

    // ===== Constructor =====

    public PersonDAO(EntityManagerFactory emf) {
        super(emf, Person.class);
    }
}
