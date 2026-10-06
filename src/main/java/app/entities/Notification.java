package app.entities;

import app.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Notification {

    // ===== Fields =====

    @Id
    @GeneratedValue
    Integer id;

    @Setter
    String title;

    @Setter
    @Enumerated(EnumType.STRING)
    NotificationType notificationType;

    @Setter
    Integer targetId;

    @Setter
    String targetType;

    @Setter
    boolean read;

    LocalDateTime createdAt;

    // ===== Relations =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;


    // ===== JPA Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        setCreatedDate();
    }

    @PreUpdate
    protected void onUpdate() {
    }

    public void setCreatedDate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
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
        Notification notification = (Notification) o;
        return getId() != null && Objects.equals(getId(), notification.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
