package app.entities;

import app.entities.generics.LinkableEntity;
import app.enums.Relation;
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
public class Person implements LinkableEntity {

    // ===== Fields =====

    @Id
    @GeneratedValue
    Integer personId;

    @Setter
    String name;

    @Setter
    String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    Relation relation;

    @Enumerated(EnumType.STRING)
    @Setter
    private Visibility visibility;

    LocalDate createdAt;
    LocalDate updatedAt;

    // ===== Relations =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    User user;

    // ===== LinkableEntity =====

    @Override
    public Integer getId() {
        return personId;
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

    public void setDefaultVisibility () {
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
        Person person = (Person) o;
        return getPersonId() != null && Objects.equals(getPersonId(), person.getPersonId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
