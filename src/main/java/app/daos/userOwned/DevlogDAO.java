package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Devlog;
import app.entities.Person;
import jakarta.persistence.EntityManagerFactory;

public class DevlogDAO extends UserOwnedDAO<Devlog, Integer> {

    // ===== Constructor =====

    public DevlogDAO(EntityManagerFactory emf) {
        super(emf, Devlog.class);
    }
}