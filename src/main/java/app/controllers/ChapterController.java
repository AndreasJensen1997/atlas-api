package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.chapter.ChapterRequestDTO;
import app.dtos.chapter.ChapterResponseDTO;
import app.entities.Chapter;
import app.mappers.ChapterMapper;
import app.services.ChapterService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;

public class ChapterController extends AbstractController<ChapterRequestDTO, ChapterResponseDTO, Chapter, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    private final ChapterService chapterService;
    private final ChapterMapper chapterMapper;

    // ===== Constructor =====

    public ChapterController(ChapterService chapterService, ChapterMapper chapterMapper) {
        this.chapterService = chapterService;
        this.chapterMapper = chapterMapper;
    }

    // ===== Request Handling =====

    @Override
    protected ChapterRequestDTO parseBody(Context ctx) {
        return ctx.bodyValidator(ChapterRequestDTO.class)
                .check(req -> req.title() != null && !req.title().isBlank(), "Title cannot be blank")
                .check(req -> req.subtitle() != null && !req.subtitle().isBlank(), "Subtitle cannot be blank")
                .check(req -> req.startDate() == null || req.endDate() == null || !req.endDate().isBefore(req.startDate()), "End date cannot be before start date")
                .get();
    }

    // ===== Entity Operations =====

    @Override
    protected Chapter createEntity(ChapterRequestDTO dto, Integer userId) {
        return chapterService.createChapter(dto, userId);
    }

    @Override
    protected Chapter fetchEntityById(Integer chapterId, Integer userId) {
        return chapterService.getById(chapterId, userId);
    }

    @Override
    protected List<Chapter> fetchAllByUserId(Integer userId) {
        return chapterService.getAllById(userId);
    }

    @Override
    protected Chapter updateEntity(Integer entityId, ChapterRequestDTO chapterRequestDTO, Integer userId) {
        return chapterService.update(entityId, chapterRequestDTO, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        chapterService.delete(entityId, userId);
    }

    @Override
    protected Chapter getRandom(Integer userId) {
        return chapterService.getRandom(userId);
    }

    // ===== Response Mapping =====

    @Override
    protected ChapterResponseDTO mapToResponse(Chapter entity) {
        return chapterMapper.toResponse(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/chapters", this::create);
        get("/api/chapters/random", this::randomByUserId);
        get("/api/chapters/{id}", this::getById);
        get("/api/chapters", this::getAllById);
        put("/api/chapters/{id}", this::updateById);
        delete("/api/chapters/{id}", this::deleteById);
    }
}