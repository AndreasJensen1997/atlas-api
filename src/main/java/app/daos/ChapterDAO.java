package app.daos;

import app.entities.AppUser;
import app.entities.Chapter;
import jakarta.persistence.EntityManagerFactory;

public class ChapterDAO extends AbstractDAO<Chapter, Integer> {

    public ChapterDAO(EntityManagerFactory emf) {
        super(emf, Chapter.class);
    }






}