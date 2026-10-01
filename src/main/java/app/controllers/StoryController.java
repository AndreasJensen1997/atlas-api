package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.story.StoryRequestDTO;
import app.dtos.story.StoryResponseDTO;
import app.entities.Story;
import app.mappers.StoryMapper;
import app.services.StoryService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;

public class StoryController extends AbstractController<StoryRequestDTO, StoryResponseDTO, Story, Integer> implements EndpointGroup {

    private final StoryService storyService;
    private final StoryMapper storyMapper;

    public StoryController(StoryService storyService, StoryMapper storyMapper) {
        this.storyService = storyService;
        this.storyMapper = storyMapper;
    }

    // Creates requestDTO from context
    @Override
    protected StoryRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(StoryRequestDTO.class);
    }

    // Fetches story from id
    @Override
    protected Story fetchEntityById(Integer storyId, Integer userId) {
        return storyService.getById(storyId, userId);
    }

    // Fetches all stories from user id
    @Override
    protected List<Story> fetchAllByUserId(Integer userId) {
        return storyService.getAllById(userId);
    }

    // Parses id from string to int from url
    @Override
    protected Integer parseId(String idStr) {
        return Integer.parseInt(idStr);
    }

    // Persists entity to DB
    @Override
    protected Story createEntity(StoryRequestDTO dto, Integer userId) {
        return storyService.createStory(dto, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        storyService.delete(entityId, userId);
    }

    @Override
    protected Story updateEntity(Integer entityId, StoryRequestDTO storyRequestDTO, Integer userId) {
        return storyService.update(entityId, storyRequestDTO, userId);
    }

    // Maps entity to responseDTO
    @Override
    protected StoryResponseDTO mapToResponse(Story entity) {
        return storyMapper.toResponse(entity);
    }

    // Adds endpoints
    @Override
    public void addEndpoints() {
        post("/api/stories", this::create);
        get("/api/stories/{id}", this::getById);
        get("/api/stories", this::getAllById);
        delete("/api/stories/{id}", this::deleteById);
        put("/api/stories/{id}", this::updateById);
    }
}