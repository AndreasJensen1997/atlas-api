package app.controllers;

import app.dtos.Mention.MentionRequestDTO;
import app.dtos.Mention.MentionResponseDTO;
import app.enums.TargetType;
import app.services.MentionService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MentionController implements EndpointGroup {

    private final MentionService mentionService;

    public MentionController(MentionService mentionService) {
        this.mentionService = mentionService;
    }

    public void create(Context ctx) {
        MentionRequestDTO dto = ctx.bodyAsClass(MentionRequestDTO.class);
        MentionResponseDTO created = mentionService.createMention(dto);
        ctx.status(201).json(created);
    }

    public void getOutgoing(Context ctx) {
        TargetType ownerType = TargetType.valueOf(ctx.pathParam("ownerType").toUpperCase());
        int ownerId = Integer.parseInt(ctx.pathParam("ownerId"));
        List<MentionResponseDTO> mentions = mentionService.getOutgoingMentions(ownerType, ownerId);
        ctx.json(mentions);
    }

    public void getIncoming(Context ctx) {
        TargetType targetType = TargetType.valueOf(ctx.pathParam("targetType").toUpperCase());
        int targetId = Integer.parseInt(ctx.pathParam("targetId"));
        List<MentionResponseDTO> mentions = mentionService.getIncomingMentions(targetType, targetId);
        ctx.json(mentions);
    }

    public void deleteEntity(Context ctx) {
        int mentionId = Integer.parseInt(ctx.pathParam("id"));
        mentionService.deleteMention(mentionId);
        ctx.status(204);
    }

    @Override
    public void addEndpoints() {
        post("/api/mentions", this::create);
        get("/api/mentions/owner/{ownerType}/{ownerId}", this::getOutgoing);
        get("/api/mentions/target/{targetType}/{targetId}", this::getIncoming);
        delete("/api/mentions/{id}", this::deleteEntity);
    }
}