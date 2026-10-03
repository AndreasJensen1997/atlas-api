package app.dtos.entityList;

import app.enums.Visibility;

import java.time.LocalDate;
import java.util.List;

public record EntityListRequestDTO(
        String title,
        String subtitle,
        Visibility visibility,
        List<String> items
) {
}
