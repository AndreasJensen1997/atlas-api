package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.place.PlaceRequestDTO;
import app.dtos.place.PlaceResponseDTO;
import app.entities.Place;
import app.mappers.PlaceMapper;
import app.services.PlaceService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class PlaceController extends AbstractController<PlaceRequestDTO, PlaceResponseDTO, Place, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    private final PlaceService placeService;
    private final PlaceMapper placeMapper;

    // ===== Constructor =====

    public PlaceController(PlaceService placeService, PlaceMapper placeMapper) {
        this.placeService = placeService;
        this.placeMapper = placeMapper;
    }

    // ===== Request Handling =====

    @Override
    protected PlaceRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(PlaceRequestDTO.class);
    }

    // ===== Entity Operations =====

    @Override
    protected Place createEntity(PlaceRequestDTO dto, Integer userId) {
        return placeService.createPlace(dto, userId);
    }
    @Override
    protected Place fetchEntityById(Integer placeId, Integer userId) {
        return placeService.getById(placeId, userId);
    }

    @Override
    protected List<Place> fetchAllByUserId(Integer userId) {
        return placeService.getAllById(userId);
    }

    @Override
    protected Place updateEntity(Integer entityId, PlaceRequestDTO placeRequestDTO, Integer userId) {
        return placeService.update(entityId, placeRequestDTO, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        placeService.delete(entityId, userId);
    }

    @Override
    protected Place getRandom(Integer userId) {
        return placeService.getRandom(userId);
    }

    // ===== Response Mapping =====

    @Override
    protected PlaceResponseDTO mapToResponse(Place entity) {
        return placeMapper.toResponse(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/places", this::create);
        get("/api/places/random", this::randomByUserId);
        get("/api/places/{id}", this::getById);
        get("/api/places", this::getAllById);
        put("/api/places/{id}", this::updateById);
        delete("/api/places/{id}", this::deleteById);
    }
}