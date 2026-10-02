package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Chapter;
import jakarta.persistence.EntityManagerFactory;

public class ChapterDAO extends UserOwnedDAO<Chapter, Integer> {

    // ===== Constructor =====

    public ChapterDAO(EntityManagerFactory emf) {
        super(emf, Chapter.class);
    }
}