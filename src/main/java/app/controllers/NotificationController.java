package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.notification.NotificationRequestDTO;
import app.dtos.notification.NotificationResponseDTO;
import app.entities.Notification;
import app.exceptions.ApiException;
import app.mappers.NotificationMapper;
import app.services.NotificationService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class NotificationController extends AbstractController<NotificationRequestDTO, NotificationResponseDTO, Notification, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    // ===== Constructor =====

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    // ===== Request Handling =====

    @Override
    protected NotificationRequestDTO parseBody(Context ctx) {
        return ctx.bodyValidator(NotificationRequestDTO.class)
                .check(req -> req.title() != null && !req.title().isBlank(), "Title cannot be blank")
                .check(req -> req.notificationType() != null, "Notification type cannot be null")
                .get();
    }

    // ===== Entity Operations =====

    @Override
    protected Notification createEntity(NotificationRequestDTO dto, Integer userId) {
        return notificationService.createNotification(dto, userId);
    }

    @Override
    protected Notification fetchEntityById(Integer notificationId, Integer userId) {
        return notificationService.getById(notificationId, userId);
    }

    @Override
    protected List<Notification> fetchAllByUserId(Integer userId) {
        return notificationService.getAllById(userId);
    }

    @Override
    protected Notification getRandom(Integer userId) {
        throw new ApiException(405, "Get random feature not supported for notifications");
    }

    @Override
    protected Notification updateEntity(Integer notificationId, NotificationRequestDTO dto, Integer userId) {
        return notificationService.update(notificationId, dto, userId);
    }

    @Override
    protected void deleteEntity(Integer notificationId, Integer userId) {
        notificationService.delete(notificationId, userId);
    }

    // ===== Response Mapping =====

    @Override
    protected NotificationResponseDTO mapToResponse(Notification entity) {
        return notificationMapper.toResponse(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/notifications", this::create);
        get("/api/notifications/{id}", this::getById);
        get("/api/notifications", this::getAllById);
        put("/api/notifications/{id}", this::updateById);
        delete("/api/notifications/{id}", this::deleteById);
    }
}