package app.daos;

import app.entities.AppUser;
import app.entities.List;
import jakarta.persistence.EntityManagerFactory;

public class ListDAO extends AbstractDAO <List, Integer> {


    public ListDAO(EntityManagerFactory emf) {
        super(emf, List.class);
    }

}
