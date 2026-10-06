package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.chapter.ChapterRequestDTO;
import app.dtos.devlog.DevlogRequestDTO;
import app.dtos.devlog.DevlogResponseDTO;
import app.entities.Chapter;
import app.entities.Devlog;
import app.exceptions.ApiException;
import app.mappers.DevlogMapper;
import app.services.DevlogService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

public class DevlogController extends AbstractController<DevlogRequestDTO, DevlogResponseDTO, Devlog, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    DevlogService devlogService;
    DevlogMapper devlogMapper;

    // ===== Constructor =====

    public DevlogController(DevlogService devlogService, DevlogMapper devlogMapper) {
        this.devlogService = devlogService;
        this.devlogMapper = devlogMapper;
    }

    // ===== Request Handling =====

    @Override
    protected DevlogRequestDTO parseBody(Context ctx) {
        return ctx.bodyValidator(DevlogRequestDTO.class)
                .check(req -> req.title() != null && !req.title().isBlank(), "Title cannot be blank")
                .check(req -> req.subtitle() != null && !req.subtitle().isBlank(), "Subtitle cannot be blank")
                .check(req -> req.content() != null && !req.content().isBlank(), "Content cannot be blank")
                .get();
    }

    // ===== Entity Operations =====

    @Override
    protected Devlog createEntity(DevlogRequestDTO dto, Integer userId) {
        return devlogService.createDevlog(dto,userId);
    }

    @Override
    protected Devlog fetchEntityById(Integer devlogId, Integer userId) {
        return devlogService.getById(devlogId,userId);
    }

    @Override
    protected List<Devlog> fetchAllByUserId(Integer userId) {
        return devlogService.getAllById(userId);
    }

    @Override
    protected Devlog getRandom(Integer userId) {
        throw new ApiException(405, "Get random feature not supported for fragments");
    }

    @Override
    protected Devlog updateEntity(Integer devlogId, DevlogRequestDTO dto, Integer userId) {
        return devlogService.update(devlogId,dto,userId);
    }

    @Override
    protected void deleteEntity(Integer devlogId, Integer userId) {
        devlogService.delete(devlogId, userId);

    }

    // ===== Response Mapping =====

    @Override
    protected DevlogResponseDTO mapToResponse(Devlog entity) {
        return devlogMapper.toResponse(entity);
    }

    @Override
    public void addEndpoints() {
        post("/api/devlogs", this::create);
        get("/api/devlogs/{id}", this::getById);
        get("/api/devlogs", this::getAllById);
        put("/api/devlogs/{id}", this::updateById);
        delete("/api/devlogs/{id}", this::deleteById);

    }
}
