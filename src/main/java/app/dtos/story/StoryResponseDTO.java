package app.dtos.story;

import app.enums.Visibility;

import java.time.LocalDate;

public record StoryResponseDTO(
        Integer storyId,
        String title,
        String subtitle,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate createdAt,
        LocalDate updatedAt,
        Visibility visibility
) {}
