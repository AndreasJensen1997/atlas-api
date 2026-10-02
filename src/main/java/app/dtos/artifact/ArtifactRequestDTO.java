package app.dtos.artifact;

import app.entities.ArtifactType;
import app.enums.Visibility;

public record ArtifactRequestDTO(
        String title,
        String subtitle,
        String content,
        Visibility visibility,
        ArtifactType artifactType
) {
}
