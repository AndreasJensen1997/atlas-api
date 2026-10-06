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

    // ===== Fields =====

    @Id
    @GeneratedValue
    private Integer id;

    @Setter
    private String title;

    @Setter
    private String subtitle;

    @Setter
    private String content;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    @Setter
    @Enumerated(EnumType.STRING)
    private Visibility visibility;


    // ===== Relations =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artifact_type_id", nullable = false)
    @Setter
    private ArtifactType artifactType;

    // ===== LinkableEntity =====

    @Override
    public Integer getId() {
        return id;
    }

    // ===== JPA Lifecycle Callbacks =====

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

    // ===== Equals & HashCode =====

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
        return getId() != null && Objects.equals(getId(), artifact.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
