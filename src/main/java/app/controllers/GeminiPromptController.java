package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.geminiPrompt.GeminiPromptRequestDTO;
import app.dtos.geminiPrompt.GeminiPromptResponseDTO;
import app.dtos.geminiPrompt.GeminiPromptSaveDTO;
import app.entities.GeminiPrompt;
import app.exceptions.ApiException;
import app.services.GeminiPromptService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GeminiPromptController extends AbstractController<GeminiPromptSaveDTO, GeminiPromptResponseDTO, GeminiPrompt, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    private final GeminiPromptService geminiPromptService;

    // ===== Constructor =====

    public GeminiPromptController(GeminiPromptService geminiPromptService) {
        this.geminiPromptService = geminiPromptService;
    }

    // ===== Custom Operations =====

    public void generatePrompt(Context ctx) throws JsonProcessingException {
        getUserIdOrThrow(ctx);

        GeminiPromptRequestDTO requestDTO = ctx.bodyValidator(GeminiPromptRequestDTO.class)
                .check(req -> req.prompt() != null && !req.prompt().isBlank(), "Prompt text cannot be empty.")
                .get();

        String generatedText = geminiPromptService.askGemini(requestDTO.prompt());

        ctx.status(200).json(Map.of("promptText", generatedText));
    }

    // ===== Request Handling =====

    @Override
    protected GeminiPromptSaveDTO parseBody(Context ctx) {
        return ctx.bodyValidator(GeminiPromptSaveDTO.class)
                .check(req -> req.content() != null && !req.content().isBlank(), "Content cannot be blank")
                .get();
    }

    // ===== Entity Operations =====

    @Override
    protected GeminiPrompt createEntity(GeminiPromptSaveDTO dto, Integer userId) {
        return geminiPromptService.savePromptText(dto.content(), userId);
    }

    @Override
    protected GeminiPrompt fetchEntityById(Integer promptId, Integer userId) {
        return geminiPromptService.getById(promptId, userId);
    }

    @Override
    protected List<GeminiPrompt> fetchAllByUserId(Integer userId) {
        return geminiPromptService.getAllById(userId);
    }

    @Override
    protected GeminiPrompt updateEntity(Integer promptId, GeminiPromptSaveDTO dto, Integer userId) {
        throw new ApiException(405, "Updating prompt history is not supported.");
    }

    @Override
    protected GeminiPrompt getRandom(Integer userId) {
        throw new ApiException(405, "Get random feature not supported for prompts.");
    }

    @Override
    protected void deleteEntity(Integer promptId, Integer userId) {
        geminiPromptService.delete(promptId, userId);
    }

    // ===== Response Mapping =====

    @Override
    protected GeminiPromptResponseDTO mapToResponse(GeminiPrompt entity) {
        return new GeminiPromptResponseDTO(entity.getId(), entity.getContent());
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/gemini-prompts/generate", this::generatePrompt);
        post("/api/gemini-prompts", this::create);
        get("/api/gemini-prompts/{id}", this::getById);
        get("/api/gemini-prompts", this::getAllById);
        delete("/api/gemini-prompts/{id}", this::deleteById);
    }
}