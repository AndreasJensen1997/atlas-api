package app.dtos.GeminiPrompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponseDTO(@JsonProperty("candidates") List<Candidate> candidates) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(
            @JsonProperty("content") Content content
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(
            @JsonProperty("parts") List<Part> parts
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(
            @JsonProperty("text") String text
    ) {}
}