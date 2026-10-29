package app.services;

import app.daos.userOwned.NotificationDAO;
import app.dtos.notification.NotificationRequestDTO;
import app.entities.Devlog;
import app.entities.Notification;
import app.entities.User;
import app.enums.NotificationType;
import app.exceptions.ApiException;
import app.mappers.NotificationMapper;
import app.utils.websocket.ConnectionRegistry;
import lombok.extern.slf4j.Slf4j;
import io.javalin.http.HttpStatus;
import java.util.List;


@Slf4j
public class NotificationService {

    // ===== Dependencies =====

    private final NotificationDAO notificationDAO;
    private final UserService userService;
    private final NotificationMapper notificationMapper;
    private final ConnectionRegistry connectionRegistry;


    // ===== Constructor =====

    public NotificationService(NotificationDAO notificationDAO, UserService userService, NotificationMapper notificationMapper, ConnectionRegistry connectionRegistry) {
        this.notificationDAO = notificationDAO;
        this.userService = userService;
        this.notificationMapper = notificationMapper;
        this.connectionRegistry = connectionRegistry;
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

        if (!notification.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Notification not found with ID: " + notificationId);
        }

        return notification;
    }

    public List<Notification> getAllById(int userId) {
        return notificationDAO.getAllByUserId(userId);
    }

    // ===== Update =====

    public Notification update(Integer notificationId, NotificationRequestDTO dto, int userId) {
        Notification notification = getById(notificationId, userId);

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

    // ===== Custom methods =====

    public void sendNotification(Notification notification) {
        Notification saved = notificationDAO.create(notification);
        connectionRegistry.send(saved.getUser().getId(), notificationMapper.toResponse(saved));
    }

    public void notifyAllUsers(Devlog devlog) {
        List<User> allUsers = userService.getAll();

        for (User user : allUsers) {
            try {
                Notification notification = Notification.builder()
                        .title("A new devlog : \"" + devlog.getTitle() + "\" has been released")
                        .notificationType(NotificationType.DEVLOG)
                        .targetId(devlog.getId())
                        .targetType("DEVLOG")
                        .user(user)
                        .build();

                sendNotification(notification);

            } catch (Exception e) {
                log.error("Could not notify user {} about devlog {}", user.getId(), devlog.getId(), e);
            }
        }
    }

}