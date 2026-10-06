package app.services;

import app.daos.userOwned.NotificationDAO;
import app.dtos.notification.NotificationRequestDTO;
import app.entities.Notification;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.NotificationMapper;

import java.util.List;

public class NotificationService {

    // ===== Dependencies =====

    private final NotificationDAO notificationDAO;
    private final UserService userService;
    private final NotificationMapper notificationMapper;

    // ===== Constructor =====

    public NotificationService(NotificationDAO notificationDAO, UserService userService, NotificationMapper notificationMapper) {
        this.notificationDAO = notificationDAO;
        this.userService = userService;
        this.notificationMapper = notificationMapper;
    }

    // ===== Create =====

    public Notification createNotification(NotificationRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        Notification newNotification = notificationMapper.toEntity(dto, owner);

        return notificationDAO.create(newNotification);
    }

    // ===== Read =====

    public Notification getById(Integer notificationId, int userId) {
        Notification notification = notificationDAO.getById(notificationId);

        if (notification == null) {
            throw new IllegalArgumentException("Notification not found with ID: " + notificationId);
        }

        if (!notification.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Notification not found with ID: " + notificationId);
        }

        return notification;
    }

    public List<Notification> getAllById(int userId) {
        return notificationDAO.getAllByUserId(userId);
    }

    // ===== Update =====

    public Notification update(Integer notificationId, NotificationRequestDTO dto, int userId) {
        Notification notification = notificationDAO.getById(notificationId);

        if (notification == null) {
            throw new IllegalArgumentException("Notification not found with ID: " + notificationId);
        }

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this notification.");
        }

        notification.setTitle(dto.title());
        notification.setNotificationType(dto.notificationType());
        notification.setTargetId(dto.targetId());
        notification.setTargetType(dto.targetType());
        notification.setRead(dto.read());

        return notificationDAO.update(notification);
    }

    public Notification markAsRead(Integer notificationId, int userId) {
        Notification notification = getById(notificationId, userId);
        notification.setRead(true);

        return notificationDAO.update(notification);
    }

    // ===== Delete =====

    public void delete(Integer notificationId, int userId) {
        Notification notification = getById(notificationId, userId);

        notificationDAO.delete(notification.getId());
    }
}