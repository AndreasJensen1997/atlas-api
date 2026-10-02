package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Story;
import jakarta.persistence.EntityManagerFactory;

public class StoryDAO extends UserOwnedDAO<Story, Integer> {

    // ===== Constructor =====

    public StoryDAO(EntityManagerFactory emf) {
        super(emf, Story.class);
    }
}
