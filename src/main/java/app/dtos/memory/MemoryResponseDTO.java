package app.dtos.memory;

import app.enums.Visibility;

import java.time.LocalDate;

public record MemoryResponseDTO(
        Integer memoryId,
        String title,
        String subtitle,
        String content,
        LocalDate date,
        LocalDate createdAt,
        LocalDate updatedAt,
        Visibility visibility) {
}
