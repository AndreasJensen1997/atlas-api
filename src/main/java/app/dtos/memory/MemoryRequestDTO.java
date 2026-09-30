package app.dtos.memory;

import app.enums.Visibility;

import java.time.LocalDate;

public record MemoryRequestDTO(
        String title,
        String subtitle,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        Visibility visibility
) {}