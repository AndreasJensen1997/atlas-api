package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.Place.PlaceRequestDTO;
import app.dtos.Place.PlaceResponseDTO;
import app.entities.Place;
import app.mappers.PlaceMapper;
import app.services.PlaceService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class PlaceController extends AbstractController<PlaceRequestDTO, PlaceResponseDTO, Place, Integer> implements EndpointGroup {

    private final PlaceService placeService;
    private final PlaceMapper placeMapper;

    public PlaceController(PlaceService placeService, PlaceMapper placeMapper) {
        this.placeService = placeService;
        this.placeMapper = placeMapper;
    }

    @Override
    protected PlaceRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(PlaceRequestDTO.class);
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
    protected Place getRandom(Integer userId) {
        return placeService.getRandom(userId);
    }

    @Override
    protected Integer parseId(String idStr) {
        return Integer.parseInt(idStr);
    }

    @Override
    protected Place createEntity(PlaceRequestDTO dto, Integer userId) {
        return placeService.createPlace(dto, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        placeService.delete(entityId, userId);
    }

    @Override
    protected Place updateEntity(Integer entityId, PlaceRequestDTO placeRequestDTO, Integer userId) {
        return placeService.update(entityId, placeRequestDTO, userId);
    }

    @Override
    protected PlaceResponseDTO mapToResponse(Place entity) {
        return placeMapper.toResponseDTO(entity);
    }

    @Override
    public void addEndpoints() {
        post("/api/places", this::create);
        get("/api/places/random", this::randomByUserId);
        get("/api/places/{id}", this::getById);
        get("/api/places", this::getAllById);
        delete("/api/places/{id}", this::deleteById);
        put("/api/places/{id}", this::updateById);
    }
}