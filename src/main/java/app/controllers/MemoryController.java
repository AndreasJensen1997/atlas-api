package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.memory.MemoryRequestDTO;
import app.dtos.memory.MemoryResponseDTO;
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

    private final MemoryService memoryService;
    private final MemoryMapper memoryMapper;

    public MemoryController(MemoryService memoryService, MemoryMapper memoryMapper) {
        this.memoryService = memoryService;
        this.memoryMapper = memoryMapper;
    }

    // Creates requestDTO from context
    @Override
    protected MemoryRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(MemoryRequestDTO.class);
    }

    // Fetches memory from id
    @Override
    protected Memory fetchEntityById(Integer memoryId, Integer userId) {
        return memoryService.getById(memoryId, userId);
    }

    // Fetches all memories from user id
    @Override
    protected List<Memory> fetchAllByUserId(Integer userId) {
        return memoryService.getAllById(userId);
    }

    // Parses id from string to int from url
    @Override
    protected Integer parseId(String idStr) {
        return Integer.parseInt(idStr); // Converts the URL string to an Integer
    }

    // Persists entity to DB
    @Override
    protected Memory createEntity(MemoryRequestDTO dto, Integer userId) {
        return memoryService.createMemory(dto, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        memoryService.delete(entityId, userId);
    }

    @Override
    protected Memory updateEntity(Integer entityId, MemoryRequestDTO memoryRequestDTO, Integer userId) {
        return memoryService.update(entityId, memoryRequestDTO, userId);
    }

    // Maps entity to responseDTO
    @Override
    protected MemoryResponseDTO mapToResponse(Memory entity) {
        return memoryMapper.toResponse(entity);
    }

    // Adds endpoints
    @Override
    public void addEndpoints() {
        post("/api/memories", this::create);
        get("/api/memories/{id}", this::getById);
        get("/api/memories", this::getAllById);
        delete("/api/memories/{id}", this::deleteById);
        put("/api/memories/{id}", this::updateById);
    }
}