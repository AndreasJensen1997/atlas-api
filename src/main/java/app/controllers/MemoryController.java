package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.memory.MemoryRequestDTO;
import app.dtos.memory.MemoryResponseDTO;
import app.entities.Chapter;
import app.entities.Memory;
import app.mappers.MemoryMapper;
import app.services.MemoryService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;

public class MemoryController extends AbstractController<MemoryRequestDTO, MemoryResponseDTO, Memory, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    private final MemoryService memoryService;
    private final MemoryMapper memoryMapper;

    // ===== Constructor =====

    public MemoryController(MemoryService memoryService, MemoryMapper memoryMapper) {
        this.memoryService = memoryService;
        this.memoryMapper = memoryMapper;
    }

    // ===== Request Handling =====

    @Override
    protected MemoryRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(MemoryRequestDTO.class);
    }


    // ===== Entity Operations =====

    @Override
    protected Memory createEntity(MemoryRequestDTO dto, Integer userId) {
        return memoryService.createMemory(dto, userId);
    }

    @Override
    protected Memory fetchEntityById(Integer memoryId, Integer userId) {
        return memoryService.getById(memoryId, userId);
    }

    @Override
    protected List<Memory> fetchAllByUserId(Integer userId) {
        return memoryService.getAllById(userId);
    }

    @Override
    protected Memory updateEntity(Integer entityId, MemoryRequestDTO memoryRequestDTO, Integer userId) {
        return memoryService.update(entityId, memoryRequestDTO, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        memoryService.delete(entityId, userId);
    }

    @Override
    protected Memory getRandom(Integer userId){
        return memoryService.getRandom(userId);
    }

    // ===== Response Mapping =====

    @Override
    protected MemoryResponseDTO mapToResponse(Memory entity) {
        return memoryMapper.toResponse(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/memories", this::create);
        get("/api/memories", this::getAllById);
        get("/api/memories/random", this::randomByUserId);
        get("/api/memories/{id}", this::getById);
        put("/api/memories/{id}", this::updateById);
        delete("/api/memories/{id}", this::deleteById);
    }
}