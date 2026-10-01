package app.dtos.story;

import app.enums.Visibility;

import java.time.LocalDate;

public record StoryRequestDTO(
        String title,
        String subtitle,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        Visibility visibility
) {}
