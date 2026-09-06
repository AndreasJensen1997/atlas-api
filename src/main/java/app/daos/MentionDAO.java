package app.daos;

import app.entities.Mention;
import jakarta.persistence.EntityManagerFactory;

public class MentionDAO extends AbstractDAO<Mention, Integer> {


    public MentionDAO(EntityManagerFactory emf) {
        super(emf, Mention.class);
    }




}
