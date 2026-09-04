package app.daos;

import app.entities.Memory;
import app.entities.Story;
import jakarta.persistence.EntityManagerFactory;

public class StoryDAO extends AbstractDAO<Story, Integer> {

    public StoryDAO(EntityManagerFactory emf) {
        super(emf, Story.class);
    }
}
