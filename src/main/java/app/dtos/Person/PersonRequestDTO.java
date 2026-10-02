package app.dtos.Person;

import app.enums.Relation;
import app.enums.Visibility;

public record PersonRequestDTO(
        String name,
        String content,
        Relation relation,
        Visibility visibility
) {
}
