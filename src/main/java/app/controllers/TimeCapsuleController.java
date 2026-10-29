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
import io.javalin.http.HttpStatus;
import java.time.LocalDate;
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
        return ctx.bodyValidator(TimeCapsuleRequestDTO.class)
                .check(req -> req.title() != null && !req.title().isBlank(), "Title cannot be blank")
                .check(req -> req.unlockDate() != null, "Unlock date cannot be blank")
                .check(req -> req.unlockDate() == null || req.unlockDate().isAfter(LocalDate.now()), "Unlock date must be in the future")
                .get();
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
        throw new ApiException(HttpStatus.METHOD_NOT_ALLOWED, "Time capsules cannot be updated.");
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        timeCapsuleService.delete(entityId, userId);
    }

    @Override
    protected TimeCapsule getRandom(Integer userId) {
        throw new ApiException(HttpStatus.METHOD_NOT_ALLOWED, "Time capsules does not support random feature.");
    }

    // ===== Response Mapping =====

    @Override
    protected TimeCapsuleResponseDTO mapToResponse(TimeCapsule entity) {
        return timeCapsuleMapper.toResponse(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("time-capsules", this::create);
        get("time-capsules/random", this::randomByUserId);
        get("time-capsules/{id}", this::getById);
        get("time-capsules", this::getAllById);
        delete("time-capsules/{id}", this::deleteById);
    }
}