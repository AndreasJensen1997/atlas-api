package app.dtos.entityList;

import app.enums.Visibility;

import java.time.LocalDate;
import java.util.List;

public record EntityListResponseDTO(
        Integer listId,
        String title,
        String subtitle,
        LocalDate createdAt,
        LocalDate updatedAt,
        Integer itemAmount,
        Visibility visibility,
        List<EntityListItemDTO> items
) {
}
