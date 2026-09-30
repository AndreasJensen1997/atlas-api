package app.dtos.chapter;

import app.enums.Visibility;

import java.time.LocalDate;


public record ChapterRequestDTO(
        String title,
        String subtitle,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        Visibility visibility
) {}