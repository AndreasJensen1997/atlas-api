package app.entities;

import app.entities.generics.LinkableEntity;
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
public class Fragment implements LinkableEntity {

    // ===== Fields =====

    @Id
    @GeneratedValue
    Integer id;

    @Setter
    private String title;

    @Setter
    private String subtitle;

    @Setter
    String content;

    LocalDate createdAt;
    LocalDate updatedAt;
    int wordCount;

    // ===== RELATIONS =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    User user;

    // ===== LinkableEntity =====

    @Override
    public Integer getId() {
        return id;
    }

    // ===== JPA Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        setCreatedDate();
        calculateWordCount();
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedDate();
        calculateWordCount();
    }

    private void calculateWordCount() {
        if (content == null || content.trim().isEmpty()) {
            this.wordCount = 0;
        } else {
            this.wordCount = content.trim().split("\\s+").length;
        }
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
        Fragment fragment = (Fragment) o;
        return getId() != null && Objects.equals(getId(), fragment.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();
    }
}
