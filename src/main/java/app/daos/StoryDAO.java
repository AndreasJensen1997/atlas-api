package app.daos;

import app.entities.Memory;
import app.entities.Story;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoryDAO extends AbstractDAO<Story, Integer> {

    public StoryDAO(EntityManagerFactory emf) {
        super(emf, Story.class);
    }

}
