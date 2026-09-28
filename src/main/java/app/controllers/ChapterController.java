package app.controllers;

import app.dtos.chapter.ChapterRequestDTO;
import app.dtos.chapter.ChapterResponseDTO;
import app.entities.Chapter;
import app.mappers.ChapterMapper;
import app.services.ChapterService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.post;

public class ChapterController implements EndpointGroup {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }


    public void create(Context ctx) {
        try {
            ChapterRequestDTO dto = ctx.bodyAsClass(ChapterRequestDTO.class);

            Integer userId = ctx.attribute("currentUserId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            Chapter savedChapter = chapterService.createChapter(dto, userId);

            ChapterResponseDTO responseDto = ChapterMapper.toResponseDTO(savedChapter);

            ctx.status(201).json(responseDto);

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }


    @Override
    public void addEndpoints() {
        post("/api/chapters/create", this::create);

    }
}
