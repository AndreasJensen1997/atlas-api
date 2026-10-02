package app.mappers;

import app.dtos.Artifact.ArtifactRequestDTO;
import app.dtos.Artifact.ArtifactResponseDTO;
import app.entities.Artifact;
import app.entities.User;
import app.mappers.generics.IMapper;

public class ArtifactMapper implements IMapper<ArtifactRequestDTO, ArtifactResponseDTO, Artifact, User> {

    @Override
    public Artifact toEntity(ArtifactRequestDTO dto, User user) {
        if (dto == null) return null;
        return Artifact.builder()
                .title(dto.title())
                .subtitle(dto.subtitle())
                .content(dto.content())
                .visibility(dto.visibility())
                .artifactType(dto.artifactType())
                .user(user)
                .build();

    }

    @Override
    public ArtifactResponseDTO toResponse(Artifact artifact) {
        if (artifact == null) return null;

        return new ArtifactResponseDTO(
                artifact.getArtifactId(),
                artifact.getTitle(),
                artifact.getSubtitle(),
                artifact.getContent(),
                artifact.getCreatedAt(),
                artifact.getUpdatedAt(),
                artifact.getVisibility(),
                artifact.getArtifactType()
        );
    }
}