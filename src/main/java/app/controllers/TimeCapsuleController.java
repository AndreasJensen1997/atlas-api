package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.timeCapsule.TimeCapsuleRequestDTO;
import app.dtos.timeCapsule.TimeCapsuleResponseDTO;
import app.entities.TimeCapsule;
import app.exceptions.ApiException;
import app.mappers.TimeCapsuleMapper;
import app.services.TimeCapsuleService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TimeCapsuleController extends AbstractController<TimeCapsuleRequestDTO, TimeCapsuleResponseDTO, TimeCapsule, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    private final TimeCapsuleService timeCapsuleService;
    private final TimeCapsuleMapper timeCapsuleMapper;

    // ===== Constructor =====

    public TimeCapsuleController(TimeCapsuleService timeCapsuleService, TimeCapsuleMapper timeCapsuleMapper) {
        this.timeCapsuleService = timeCapsuleService;
        this.timeCapsuleMapper = timeCapsuleMapper;
    }

    // ===== Request Handling =====

    @Override
    protected TimeCapsuleRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(TimeCapsuleRequestDTO.class);
    }

    @Override
    protected Integer parseId(String idStr) {
        return Integer.parseInt(idStr);
    }

    // ===== Entity Operations =====

    @Override
    protected TimeCapsule createEntity(TimeCapsuleRequestDTO dto, Integer userId) {
        return timeCapsuleService.createTimeCapsule(dto, userId);
    }

    @Override
    protected TimeCapsule fetchEntityById(Integer timeCapsuleId, Integer userId) {
        return timeCapsuleService.getById(timeCapsuleId, userId);
    }

    @Override
    protected List<TimeCapsule> fetchAllByUserId(Integer userId) {
        return timeCapsuleService.getAllById(userId);
    }

    @Override
    protected TimeCapsule updateEntity(Integer entityId, TimeCapsuleRequestDTO dto, Integer userId) {
        throw new ApiException(405, "Time capsules cannot be updated.");
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        timeCapsuleService.delete(entityId, userId);
    }

    @Override
    protected TimeCapsule getRandom(Integer userId) {
        throw new ApiException(405, "Time capsules does not support random feature.");
    }

    // ===== Response Mapping =====

    @Override
    protected TimeCapsuleResponseDTO mapToResponse(TimeCapsule entity) {
        return timeCapsuleMapper.toResponse(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/timecapsules", this::create);
        get("/api/timecapsules/random", this::randomByUserId);
        get("/api/timecapsules/{id}", this::getById);
        get("/api/timecapsules", this::getAllById);
        delete("/api/timecapsules/{id}", this::deleteById);
    }
}