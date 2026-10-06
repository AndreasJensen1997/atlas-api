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
public class Memory implements LinkableEntity {

    // ===== Fields =====

    @Id
    @GeneratedValue
    Integer id;

    @Setter
    String title;

    @Setter
    String subtitle;

    @Setter
    String content;

    @Setter
    LocalDate date;

    LocalDate createdAt;
    LocalDate updatedAt;

    @Setter
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    // ===== Relations =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

    // ===== LinkableEntity =====

    @Override
    public Integer getId() {
        return id;
    }

    // ===== JPA Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        setDefaultVisibility();
        setCreatedDate();
        setUpdatedDate();
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedDate();
    }

    public void setDefaultVisibility() {
        if (this.visibility == null) {
            this.visibility = Visibility.PRIVATE;
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
        Memory memory = (Memory) o;
        return getId() != null && Objects.equals(getId(), memory.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
