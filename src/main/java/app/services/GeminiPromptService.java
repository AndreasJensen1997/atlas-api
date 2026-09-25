package app.services;

import app.daos.userOwned.GeminiPromptDAO;
import app.dtos.GeminiPrompt.GeminiResponseDTO;
import app.entities.AppUser;
import app.entities.GeminiPrompt;
import app.mappers.GeminiPromptMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class GeminiPromptService {

    ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiPromptDAO geminiPromptDAO;

    String apiKey = System.getenv("GEMINI_API_KEY");

    public GeminiPromptService(GeminiPromptDAO geminiPromptDAO) {
        this.geminiPromptDAO = geminiPromptDAO;
    }

    public String geminiRequest(String prompt) throws JsonProcessingException {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        String jsonBody = null;
        jsonBody = objectMapper.writeValueAsString(body);

        String model = "gemini-3.5-flash-lite";
        String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model
                        + ":generateContent";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.body();
    }

    public String askGemini(String userInput) throws JsonProcessingException {
        String rawJson = geminiRequest(userInput);

        try {
            GeminiResponseDTO responseDto = objectMapper.readValue(rawJson, GeminiResponseDTO.class);

            GeminiPrompt tempPrompt = GeminiPromptMapper.toEntity(responseDto);

            if (tempPrompt == null || tempPrompt.getContent() == null) {
                throw new RuntimeException("Could not extract text from Gemini response.");
            }
            return tempPrompt.getContent();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    public GeminiPrompt savePromptText(String textContent, AppUser user) {
        GeminiPrompt prompt = GeminiPrompt.builder()
                .content(textContent)
                .appUser(user)
                .build();

        return geminiPromptDAO.create(prompt);
    }
}
