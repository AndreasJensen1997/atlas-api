package app.controllers;

import app.dtos.GeminiPrompt.GeminiPromptResponseDTO;
import app.dtos.GeminiPrompt.GeminiPromptSaveDTO;
import app.entities.GeminiPrompt;
import app.services.GeminiPromptService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import java.util.Map;
import static io.javalin.apibuilder.ApiBuilder.*;


public class GeminiPromptController implements EndpointGroup {


    private GeminiPromptService geminiPromptService;


    public GeminiPromptController (GeminiPromptService geminiPromptService) {
        this.geminiPromptService = geminiPromptService;

    }

    public void generatePrompt(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String userInput = body.get("prompt");

            // Just get the text from Gemini
            String generatedText = geminiPromptService.askGemini(userInput);

            // Send it back to the frontend for the user to look at
            ctx.status(200).json(Map.of("promptText", generatedText));

        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    public void savePrompt(Context ctx) {
        try {
            GeminiPromptSaveDTO dto = ctx.bodyAsClass(GeminiPromptSaveDTO.class);
            Integer currentUserId = ctx.attribute("currentUserId");

            if (currentUserId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            GeminiPrompt saved = geminiPromptService.savePromptText(dto.content(), currentUserId);

            GeminiPromptResponseDTO responseDTO = new GeminiPromptResponseDTO(saved.getGeminiPromptId(), saved.getContent());

            ctx.status(201).json(responseDTO);

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    @Override
    public void addEndpoints() {
        post("/api/generatePrompt", this::generatePrompt);
        post("/api/savePrompt", this::savePrompt);
    }
}
