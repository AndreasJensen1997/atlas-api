package app.services;

import app.daos.userOwned.GeminiPromptDAO;
import app.dtos.geminiPrompt.GeminiResponseDTO;
import app.entities.Chapter;
import app.entities.User;
import app.entities.GeminiPrompt;
import app.exceptions.ApiException;
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

    // ===== Dependencies =====

    ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiPromptDAO geminiPromptDAO;
    private final UserService userService;
    String apiKey = System.getenv("GEMINI_API_KEY");

    // ===== Constructor =====

    public GeminiPromptService(GeminiPromptDAO geminiPromptDAO, UserService userService) {
        this.geminiPromptDAO = geminiPromptDAO;
        this.userService = userService;
    }

    // ===== Gemini API =====

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

    // ===== Response Processing =====

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

    // ===== Create =====

    public GeminiPrompt savePromptText(String textContent, Integer userId) {
        User user = userService.getById(userId);

        GeminiPrompt prompt = GeminiPrompt.builder()
                .content(textContent)
                .user(user)
                .build();

        return geminiPromptDAO.create(prompt);
    }

    // ===== Read =====

    public GeminiPrompt getById(Integer geminiPromptId, int userId) {
        GeminiPrompt geminiPrompt = geminiPromptDAO.getById(geminiPromptId);

        if (geminiPrompt == null) {
            throw new IllegalArgumentException("Chapter not found with ID: " + geminiPromptId);
        }

        if (!geminiPrompt.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Prompt not found with ID: " + geminiPromptId);
        }
        return geminiPrompt;
    }

    public List<GeminiPrompt> getAllById(int userId) {

        return geminiPromptDAO.getAllByUserId(userId);
    }

    // ===== Delete =====

    public void delete(Integer chapterId, int userId) {
        GeminiPrompt geminiPrompt = getById(chapterId, userId);

        geminiPromptDAO.delete(geminiPrompt.getId());
    }


}
