package app.controllers;

import app.dtos.Mention.MentionRequestDTO;
import app.dtos.Mention.MentionResponseDTO;
import app.enums.TargetType;
import app.exceptions.ApiException;
import app.services.MentionService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MentionController implements EndpointGroup {

    // ===== Dependencies =====

    private final MentionService mentionService;

    // ===== Constructor =====

    public MentionController(MentionService mentionService) {
        this.mentionService = mentionService;
    }

    // ===== Helper methods =====

    private int getUserId(Context ctx) {
        Integer userId = ctx.attribute("userId");
        if (userId == null) {
            throw new ApiException(401, "Unauthorized");
        }
        return userId;
    }

    // ===== Entity Operations =====

    public void getIncoming(Context ctx) {
        TargetType targetType = TargetType.valueOf(ctx.pathParam("targetType").toUpperCase());
        int targetId = Integer.parseInt(ctx.pathParam("targetId"));
        int userId = getUserId(ctx);

        if (targetId <= 0) {
            throw new IllegalArgumentException("ID must be a positive integer.");
        }
        List<MentionResponseDTO> mentions = mentionService.getIncomingMentions(targetType, targetId, userId);
        ctx.json(mentions);
    }

    public void getOutgoing(Context ctx) {
        TargetType ownerType = TargetType.valueOf(ctx.pathParam("ownerType").toUpperCase());
        int ownerId = Integer.parseInt(ctx.pathParam("ownerId"));
        int userId = getUserId(ctx);

        if (ownerId <= 0) {
            throw new IllegalArgumentException("ID must be a positive integer.");
        }
        List<MentionResponseDTO> mentions = mentionService.getOutgoingMentions(ownerType, ownerId, userId);
        ctx.json(mentions);
    }

    public void create(Context ctx) {
        int userId = getUserId(ctx);

        MentionRequestDTO dto = ctx.bodyValidator(MentionRequestDTO.class)
                .check(r -> r.ownerId() != null && r.ownerId() > 0, "ownerId must be a positive integer")
                .check(r -> r.ownerType() != null, "ownerType is required")
                .check(r -> r.targetId() != null && r.targetId() > 0, "targetId must be a positive integer")
                .check(r -> r.targetType() != null, "targetType is required")
                .check(r -> r.startIndex() != null && r.endIndex() != null && r.startIndex() >= 0 && r.startIndex() < r.endIndex(), "startIndex must be >= 0 and less than endIndex")
                .get();

        MentionResponseDTO created = mentionService.createMention(dto, userId);
        ctx.status(201).json(created);
    }

    public void deleteEntity(Context ctx) {
        int mentionId = Integer.parseInt(ctx.pathParam("id"));
        int userId = getUserId(ctx);

        if (mentionId <= 0) {
            throw new ApiException(400, "ID must be a positive integer.");
        }
        mentionService.deleteMention(mentionId,userId);
        ctx.status(204);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/mentions", this::create);
        get("/api/mentions/owner/{ownerType}/{ownerId}", this::getOutgoing);
        get("/api/mentions/target/{targetType}/{targetId}", this::getIncoming);
        delete("/api/mentions/{id}", this::deleteEntity);
    }
}