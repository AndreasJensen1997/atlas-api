package app.services;

import app.daos.userOwned.NotificationDAO;
import app.daos.userOwned.TimeCapsuleDAO;
import app.dtos.timeCapsule.TimeCapsuleRequestDTO;
import app.entities.Notification;
import app.entities.TimeCapsule;
import app.entities.User;
import app.enums.NotificationType;
import app.enums.TargetType;
import app.exceptions.ApiException;
import app.mappers.TimeCapsuleMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TimeCapsuleService {

    // ===== Dependencies =====

    private final TimeCapsuleDAO timeCapsuleDAO;
    private final NotificationDAO notificationDAO;
    private final UserService userService;
    private final TimeCapsuleMapper timeCapsuleMapper;

    // ===== Constructor =====

    public TimeCapsuleService(TimeCapsuleDAO timeCapsuleDAO, NotificationDAO notificationDAO, UserService userService, TimeCapsuleMapper timeCapsuleMapper) {
        this.timeCapsuleDAO = timeCapsuleDAO;
        this.notificationDAO = notificationDAO;
        this.userService = userService;
        this.timeCapsuleMapper = timeCapsuleMapper;
    }

    // ===== Create =====

    public TimeCapsule createTimeCapsule(TimeCapsuleRequestDTO dto, int userId) {
        User owner = userService.getById(userId);
        TimeCapsule newTimeCapsule = timeCapsuleMapper.toEntity(dto, owner);

        return timeCapsuleDAO.create(newTimeCapsule);
    }

    // ===== Read =====

    public TimeCapsule getById(Integer timeCapsuleId, int userId) {
        TimeCapsule timeCapsule = timeCapsuleDAO.getById(timeCapsuleId);

        if (!timeCapsule.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Time capsule not found with ID: " + timeCapsuleId);
        }
        return timeCapsule;
    }

    public List<TimeCapsule> getAllById(int userId) {
        return timeCapsuleDAO.getAllByUserId(userId);
    }


    public List<TimeCapsule> notifyUnlockedCapsules() {
        List<TimeCapsule> foundCapsules = timeCapsuleDAO.findUnlockedAndNotNotifiedCapsules();

        for (TimeCapsule capsule : foundCapsules) {

            try {
                Notification notification = Notification.builder()
                        .title("Your time capsule \"" + capsule.getTitle() + "\" is now unlocked")
                        .notificationType(NotificationType.TIME_CAPSULE)
                        .targetId(capsule.getId())
                        .targetType("TIME_CAPSULE")
                        .user(capsule.getUser())
                        .build();

                notificationDAO.create(notification);
                capsule.setUnlockNotified(true);

                timeCapsuleDAO.update(capsule);
            } catch (Exception e) {
                log.error("Could not notify capsule {}", capsule.getId(), e);
            }

        }

        return foundCapsules;
    }

    // ===== Delete =====

    public void delete(Integer timeCapsuleId, int userId) {
        TimeCapsule timeCapsule = getById(timeCapsuleId, userId);
        timeCapsuleDAO.delete(timeCapsule.getId());
    }
}