package app.dtos.notification;

import app.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Integer notificationId,
        String title,
        NotificationType notificationType,
        Integer targetId,
        String targetType,
        boolean read,
        LocalDateTime createdAt
)
{}
