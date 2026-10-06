package app.mappers;

import app.dtos.notification.NotificationRequestDTO;
import app.dtos.notification.NotificationResponseDTO;
import app.entities.Notification;
import app.entities.User;
import app.mappers.generics.IMapper;

public class NotificationMapper implements IMapper<NotificationRequestDTO, NotificationResponseDTO, Notification, User> {

    @Override
    public Notification toEntity(NotificationRequestDTO dto, User user) {
        if (dto == null) return null;

        return Notification.builder()
                .title(dto.title())
                .notificationType(dto.notificationType())
                .targetId(dto.targetId())
                .targetType(dto.targetType())
                .read(dto.read())
                .createdAt(dto.createdAt())
                .user(user)
                .build();

    }

    @Override
    public NotificationResponseDTO toResponse(Notification notification) {
        if (notification == null) return null;

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getNotificationType(),
                notification.getTargetId(),
                notification.getTargetType(),
                notification.isRead(),
                notification.getCreatedAt()

        );
    }
}
