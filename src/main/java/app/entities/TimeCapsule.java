package app.entities;

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
public class TimeCapsule {

    // ===== Fields =====

    @Id
    @GeneratedValue
    Integer timeCapsuleId;

    @Setter
    String title;

    @Setter
    String subtitle;

    @Setter
    String content;

    @Setter
    LocalDate unlockDate;

    LocalDate dateOpened;
    boolean lockStatus;

    // ===== Relations =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    User user;

    // ===== JPA Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        isLockStatus();

    }

    @PreUpdate
    protected void onUpdate() {
        isLockStatus();

    }

    public boolean isLockStatus() {
        if (this.unlockDate == null) {
            return false;
        }
        return LocalDate.now().isBefore(this.unlockDate);
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
        TimeCapsule timeCapsule = (TimeCapsule) o;
        return getTimeCapsuleId() != null && Objects.equals(getTimeCapsuleId(), timeCapsule.getTimeCapsuleId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
