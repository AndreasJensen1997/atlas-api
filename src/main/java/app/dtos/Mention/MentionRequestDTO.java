package app.dtos.Mention;

import app.enums.TargetType;

public record MentionRequestDTO(
        Integer ownerId,
        TargetType ownerType,
        Integer startIndex,
        Integer endIndex,
        String selectedText,
        Integer targetId,
        TargetType targetType
) {
}
