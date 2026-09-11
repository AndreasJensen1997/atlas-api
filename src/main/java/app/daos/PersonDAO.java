package app.daos;

import app.entities.Memory;
import app.entities.Person;
import jakarta.persistence.EntityManagerFactory;

public class PersonDAO extends UserOwnedDAO<Person, Integer> {


    public PersonDAO(EntityManagerFactory emf) {
        super(emf, Person.class);
    }

}
