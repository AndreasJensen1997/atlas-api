package app.daos;

import app.entities.Memory;
import app.entities.TimeCapsule;
import jakarta.persistence.EntityManagerFactory;

public class TimeCapsuleDAO extends AbstractDAO<TimeCapsule, Integer> {

    public TimeCapsuleDAO(EntityManagerFactory emf) {
        super(emf, TimeCapsule.class);
    }
}
