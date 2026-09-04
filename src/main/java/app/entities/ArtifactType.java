package app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtifactType {

    @Id
    @GeneratedValue
    private Integer artifactTypeId;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Song", "Movie", "Physical Object"
}
