package app.mappers;

import app.dtos.Mention.MentionRequestDTO;
import app.dtos.Mention.MentionResponseDTO;
import app.entities.Mention;

public class MentionMapper {

    public Mention toEntity(MentionRequestDTO dto) {
        return Mention.builder()
                .ownerId(dto.ownerId())
                .ownerType(dto.ownerType())
                .startIndex(dto.startIndex())
                .endIndex(dto.endIndex())
                .selectedText(dto.selectedText())
                .targetId(dto.targetId())
                .targetType(dto.targetType())
                .build();
    }

    public MentionResponseDTO toResponse(Mention entity) {
        return new MentionResponseDTO(
                entity.getId(),
                entity.getOwnerId(),
                entity.getOwnerType(),
                entity.getStartIndex(),
                entity.getEndIndex(),
                entity.getSelectedText(),
                entity.getTargetId(),
                entity.getTargetType()
        );
    }
}