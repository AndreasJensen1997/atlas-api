package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.story.StoryRequestDTO;
import app.dtos.story.StoryResponseDTO;
import app.entities.Memory;
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

    // ===== Dependencies =====

    private final StoryService storyService;
    private final StoryMapper storyMapper;

    // ===== Constructor =====

    public StoryController(StoryService storyService, StoryMapper storyMapper) {
        this.storyService = storyService;
        this.storyMapper = storyMapper;
    }
    // ===== Request Handling =====

    @Override
    protected StoryRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(StoryRequestDTO.class);
    }


    // ===== Entity Operations =====

    @Override
    protected Story createEntity(StoryRequestDTO dto, Integer userId) {
        return storyService.createStory(dto, userId);
    }

    @Override
    protected Story fetchEntityById(Integer storyId, Integer userId) {
        return storyService.getById(storyId, userId);
    }

    @Override
    protected List<Story> fetchAllByUserId(Integer userId) {
        return storyService.getAllById(userId);
    }

    @Override
    protected Story updateEntity(Integer entityId, StoryRequestDTO storyRequestDTO, Integer userId) {
        return storyService.update(entityId, storyRequestDTO, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        storyService.delete(entityId, userId);
    }

    @Override
    protected Story getRandom(Integer userId){
        return storyService.getRandom(userId);
    }

    // ===== Response Mapping =====

    @Override
    protected StoryResponseDTO mapToResponse(Story entity) {
        return storyMapper.toResponse(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/stories", this::create);
        get("/api/stories", this::getAllById);
        get("/api/stories/random", this::randomByUserId);
        get("/api/stories/{id}", this::getById);
        put("/api/stories/{id}", this::updateById);
        delete("/api/stories/{id}", this::deleteById);
    }
}