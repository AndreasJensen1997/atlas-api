package app.dtos.person;

import app.enums.Relation;
import app.enums.Visibility;

public record PersonRequestDTO(
        String name,
        String content,
        String subtitle,
        Relation relation,
        Visibility visibility
) {
}
