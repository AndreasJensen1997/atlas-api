package app.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Artifact implements LinkableEntity {


    @Id
    @GeneratedValue
    Integer artifactId;
    String title;
    String subTitle;

    String content;
    LocalDate createdAt;


    // ===== RELATIONS =====


    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    AppUser appUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artifact_type_id", nullable = false)
    private ArtifactType artifactType;


    // 1:M
    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Mention> mentions = new HashSet<>();



    @Override
    public Integer getId() {
        return artifactId;
    }






}
