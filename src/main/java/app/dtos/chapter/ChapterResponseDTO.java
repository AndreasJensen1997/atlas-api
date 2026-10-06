package app.dtos.chapter;

import app.enums.Visibility;
import java.time.LocalDate;

public record ChapterResponseDTO(
        Integer id,
        String title,
        String subtitle,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate createdAt,
        LocalDate updatedAt,
        Visibility visibility
) {}