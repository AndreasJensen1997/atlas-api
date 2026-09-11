package app.daos;


import app.entities.Chapter;
import jakarta.persistence.EntityManagerFactory;


public class ChapterDAO extends UserOwnedDAO<Chapter, Integer> {

    public ChapterDAO(EntityManagerFactory emf) {
        super(emf, Chapter.class);
    }


    // ===== CUSTOM DAO METHODS =====

}