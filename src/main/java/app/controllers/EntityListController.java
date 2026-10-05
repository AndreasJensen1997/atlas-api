package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.entityList.EntityListRequestDTO;
import app.dtos.entityList.EntityListResponseDTO;
import app.entities.EntityList;
import app.mappers.EntityListMapper;
import app.services.EntityListService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public class EntityListController extends AbstractController<EntityListRequestDTO, EntityListResponseDTO, EntityList, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    private final EntityListService entityListService;
    private final EntityListMapper entityListMapper;

    // ===== Constructor =====

    public EntityListController(EntityListService entityListService, EntityListMapper entityListMapper) {
        this.entityListService = entityListService;
        this.entityListMapper = entityListMapper;
    }

    // ===== Request Handling =====

    @Override
    protected EntityListRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(EntityListRequestDTO.class);
    }


    // ===== Entity Operations =====

    @Override
    protected EntityList createEntity(EntityListRequestDTO dto, Integer userId) {
        return entityListService.createEntityList(dto, userId);
    }

    @Override
    protected EntityList fetchEntityById(Integer entityListId, Integer userId) {
        return entityListService.getById(entityListId, userId);
    }

    @Override
    protected List<EntityList> fetchAllByUserId(Integer userId) {
        return entityListService.getAllById(userId);
    }

    @Override
    protected EntityList updateEntity(Integer entityId, EntityListRequestDTO entityListRequestDTO, Integer userId) {
        return entityListService.update(entityId, entityListRequestDTO, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        entityListService.delete(entityId, userId);
    }

    @Override
    protected EntityList getRandom(Integer userId) {
        return entityListService.getRandom(userId);
    }

    // ===== Response Mapping =====

    @Override
    protected EntityListResponseDTO mapToResponse(EntityList entity) {
        return entityListMapper.toResponseDTO(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/entitylists", this::create);
        get("/api/entitylists/random", this::randomByUserId);
        get("/api/entitylists/{id}", this::getById);
        get("/api/entitylists", this::getAllById);
        put("/api/entitylists/{id}", this::updateById);
        delete("/api/entitylists/{id}", this::deleteById);
    }
}