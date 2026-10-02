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
public class Place implements LinkableEntity {

    // ===== Fields =====

    @Id
    @GeneratedValue
    Integer placeId;

    @Setter
    private String name;

    @Setter
    String content;

    @Setter
    private Double latitude;

    @Setter
    private Double longitude;

    @Setter
    private String address;

    @Setter
    private String city;

    @Setter
    private String country;

    LocalDate createdAt;
    LocalDate updatedAt;

    @Enumerated(EnumType.STRING)
    @Setter
    private Visibility visibility;

    // ===== Relations =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    User user;

    // ===== LinkableEntity =====

    @Override
    public Integer getId() {
        return placeId;
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
        Place place = (Place) o;
        return getPlaceId() != null && Objects.equals(getPlaceId(), place.getPlaceId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
