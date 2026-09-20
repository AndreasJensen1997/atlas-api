package app.entities;


import app.entities.generics.LinkableEntity;
import app.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Artifact implements LinkableEntity {


    @Id
    @GeneratedValue
    private Integer artifactId;
    private String title;
    private String subtitle;
    private String content;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    @Enumerated(EnumType.STRING)
    private Visibility visibility;


    // ===== RELATIONS =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private AppUser appUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artifact_type_id", nullable = false)
    private ArtifactType artifactType;


    @Override
    public Integer getId() {
        return artifactId;
    }


    // ===== JPA LIFECYCLE CALLBACKS =====
    @PrePersist
    protected void onCreate() {
        setCreatedDate();
        setDefaultVisibility();
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedDate();
    }

    public void setCreatedDate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDate.now();
        }
    }

    public void setUpdatedDate() {
        if (this.updatedAt == null) {
            this.updatedAt = LocalDate.now();
        }
    }

    public void setDefaultVisibility() {
        if (this.visibility == null) {
            this.visibility = Visibility.PRIVATE;
        }
    }


    // ===== EQUALS & HASHCODE =====
    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer()
                .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass)
            return false;
        Artifact artifact = (Artifact) o;
        return getArtifactId() != null && Objects.equals(getArtifactId(), artifact.getArtifactId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
