package app.dtos.person;

import app.enums.Relation;
import app.enums.Visibility;

import java.time.LocalDate;

public record PersonResponseDTO(
        Integer personId,
        String name,
        String content,
        Relation relation,
        Visibility visibility,
        LocalDate createdAt,
        LocalDate updatedAt
) {
}
