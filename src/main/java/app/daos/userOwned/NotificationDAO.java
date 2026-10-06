package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Devlog;
import app.entities.Notification;
import jakarta.persistence.EntityManagerFactory;

public class NotificationDAO extends UserOwnedDAO<Notification, Integer> {

    // ===== Constructor =====

    public NotificationDAO(EntityManagerFactory emf) {
        super(emf, Notification.class);
    }


}
