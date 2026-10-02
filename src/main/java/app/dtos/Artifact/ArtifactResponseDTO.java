package app.dtos.Artifact;

import app.entities.ArtifactType;
import app.enums.Visibility;

public record ArtifactResponseDTO(
        Integer artifactId,
        String title,
        String subtitle,
        String content,
        Visibility visibility,
        ArtifactType artifactType
) {
}
