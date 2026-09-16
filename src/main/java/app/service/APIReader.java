package app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class APIReader {

    ObjectMapper objectMapper = new ObjectMapper();

    String apiKey = System.getenv("GEMINI_API_KEY");


    public String geminiRequest(String prompt) {
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

    public <T> T getApiAsDTO(String url, Class<T> tclass) {
        try {
            JsonNode node = objectMapper.readTree(new URI(url).toURL().openStream());
            return objectMapper.treeToValue(node, tclass);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }


}
