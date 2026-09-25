package app.controllers;

import app.dtos.GeminiPrompt.GeminiPromptSaveDTO;
import app.dtos.GeminiPrompt.GeminiResponseDTO;
import app.entities.AppUser;
import app.entities.GeminiPrompt;
import app.services.GeminiPromptService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;


public class GeminiPromptController {


    private GeminiPromptService geminiPromptService;


    public GeminiPromptController(GeminiPromptService geminiPromptService) {
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
            AppUser currentUser = ctx.attribute("currentUser");

            if (currentUser == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            // Save the chosen text to the database
            GeminiPrompt saved = geminiPromptService.savePromptText(dto.content(), currentUser);

            ctx.status(201).json(saved);

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    public void registerRoutes(Javalin app) {
        app.post("/api/generatePrompt", this::generatePrompt);
        app.post("/api/savePrompt", this::savePrompt);
    }

}
