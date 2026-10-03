package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Devlog;
import jakarta.persistence.EntityManagerFactory;

public class NotificationDAO extends UserOwnedDAO<Devlog, Integer> {

    // ===== Constructor =====

    protected NotificationDAO(EntityManagerFactory emf, Class<Devlog> entityClass) {
        super(emf, entityClass);
    }
}
