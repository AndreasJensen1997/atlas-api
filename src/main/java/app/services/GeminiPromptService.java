package app.services;

import app.daos.userOwned.GeminiPromptDAO;
import app.dtos.geminiPrompt.GeminiResponseDTO;
import app.entities.User;
import app.entities.GeminiPrompt;
import app.exceptions.ApiException;
import app.mappers.GeminiPromptMapper;
import app.utils.general.Utils;
import app.utils.security.RateLimiter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
public class GeminiPromptService {

    // ===== Dependencies =====

    ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiPromptDAO geminiPromptDAO;
    private final UserService userService;
    private final RateLimiter rateLimiter = new RateLimiter(10, Duration.ofHours(1));
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();


    // ===== Constructor =====

    public GeminiPromptService(GeminiPromptDAO geminiPromptDAO, UserService userService) {
        this.geminiPromptDAO = geminiPromptDAO;
        this.userService = userService;
    }


    private static String loadApiKey() {
        String key;
        try {
            key = System.getenv("DEPLOYED") != null
                    ? System.getenv("GEMINI_API_KEY")
                    : Utils.getPropertyValue("GEMINI_API_KEY", "config.properties");
        } catch (ApiException e) {
            key = null;
        }
        if (key == null || key.isBlank()) {
            throw new ApiException(503, "The AI service is not configured");
        }
        return key;
    }

    // ===== Gemini API =====

    public String geminiRequest(String prompt) throws JsonProcessingException {
        String apiKey = loadApiKey();
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "maxOutputTokens", 300)
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
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new ApiException(504, "The AI service took too long to respond");
        } catch (IOException e) {
            throw new ApiException(502, "Could not reach the AI service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(503, "The request was interrupted");
        }

        int status = response.statusCode();
        if (status == 429) {
            throw new ApiException(503, "The AI service is busy right now, try again later");
        }
        if (status != 200) {
            log.error("Gemini returned status {}: {}", status, response.body());
            throw new ApiException(502, "The AI service returned an error");
        }
        return response.body();
    }

    // ===== Response Processing =====

    public String askGemini(String prompt, int userId) throws JsonProcessingException {
        rateLimiter.check(userId);
        String rawJson = geminiRequest(prompt);

        GeminiResponseDTO responseDto;
        try {
            responseDto = objectMapper.readValue(rawJson, GeminiResponseDTO.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(502, "The AI service returned an unexpected response", e);
        }

        GeminiPrompt tempPrompt = GeminiPromptMapper.toEntity(responseDto);
        if (tempPrompt == null || tempPrompt.getContent() == null) {
            throw new ApiException(502, "The AI service returned an unexpected response");
        }
        return tempPrompt.getContent();
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

        if (!geminiPrompt.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Prompt not found with ID: " + geminiPromptId);
        }
        return geminiPrompt;
    }

    public List<GeminiPrompt> getAllById(int userId) {

        return geminiPromptDAO.getAllByUserId(userId);
    }

    // ===== Delete =====

    public void delete(Integer geminiPromptId, int userId) {
        GeminiPrompt geminiPrompt = getById(geminiPromptId, userId);

        geminiPromptDAO.delete(geminiPrompt.getId());
    }


}
