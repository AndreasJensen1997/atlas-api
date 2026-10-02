package app.dtos.Artifact;

import app.entities.ArtifactType;
import app.enums.Visibility;

import java.time.LocalDate;

public record ArtifactResponseDTO(
        Integer artifactId,
        String title,
        String subtitle,
        String content,
        LocalDate createdAt,
        LocalDate updatedAt,
        Visibility visibility,
        ArtifactType artifactType
) {
}
