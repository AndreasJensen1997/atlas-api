package app.mappers;

import app.dtos.geminiPrompt.GeminiResponseDTO;
import app.entities.GeminiPrompt;

public class GeminiPromptMapper {

    public static GeminiPrompt toEntity(GeminiResponseDTO dto) {
        if (dto == null || dto.candidates() == null || dto.candidates().isEmpty()) {
            return null;
        }

        GeminiResponseDTO.Candidate firstCandidate = dto.candidates().get(0);
        if (firstCandidate == null || firstCandidate.content() == null || firstCandidate.content().parts() == null || firstCandidate.content().parts().isEmpty()) {
            return null;
        }

        String generatedText = firstCandidate.content().parts().get(0).text();

        return GeminiPrompt.builder()
                .content(generatedText)
                .build();
    }
}


