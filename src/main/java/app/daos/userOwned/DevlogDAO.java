package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Devlog;
import app.entities.Person;
import jakarta.persistence.EntityManagerFactory;

public class DevlogDAO extends UserOwnedDAO<Devlog, Integer> {

    // ===== Constructor =====

    protected DevlogDAO(EntityManagerFactory emf, Class<Devlog> entityClass) {
        super(emf, entityClass);
    }
}