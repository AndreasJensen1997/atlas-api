package app.dtos.Artifact;

import app.entities.ArtifactType;
import app.enums.Visibility;

import java.time.LocalDate;

public record ArtifactRequestDTO(
        String title,
        String subtitle,
        String content,
        Visibility visibility,
        ArtifactType artifactType
) {

}
