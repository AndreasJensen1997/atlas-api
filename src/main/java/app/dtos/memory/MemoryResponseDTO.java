package app.dtos.memory;

import app.enums.Visibility;

import java.time.LocalDate;

public record MemoryResponseDTO(
        Integer chapterId,
        String title,
        String subtitle,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        Visibility visibility) {
}
