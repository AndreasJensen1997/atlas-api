package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.chapter.ChapterRequestDTO;
import app.dtos.chapter.ChapterResponseDTO;
import app.entities.Chapter;
import app.mappers.ChapterMapper;
import app.services.ChapterService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;

public class ChapterController extends AbstractController<ChapterRequestDTO, ChapterResponseDTO, Chapter, Integer> implements EndpointGroup {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    // Creates requestDTO from context
    @Override
    protected ChapterRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(ChapterRequestDTO.class);
    }

    // Fetches chapter from id
    @Override
    protected Chapter fetchEntityById(Integer chapterId, Integer userId) {
        return chapterService.getById(chapterId, userId);
    }

    // Parses id from string to int from url
    @Override
    protected Integer parseId(String idStr) {
        return Integer.parseInt(idStr); // Converts the URL string to an Integer
    }

    // Persists entity to DB
    @Override
    protected Chapter createEntity(ChapterRequestDTO dto, Integer userId) {
        return chapterService.createChapter(dto, userId);
    }

    // Maps entity to responseDTO
    @Override
    protected ChapterResponseDTO mapToResponse(Chapter entity) {
        return ChapterMapper.toResponseDTO(entity);
    }

    // Adds endpoints
    @Override
    public void addEndpoints() {
        post("/api/chapters", this::create);
        get("/api/chapters/{id}", this::getById);
    }
}