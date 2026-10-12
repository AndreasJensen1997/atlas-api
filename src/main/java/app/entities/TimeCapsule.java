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
    Integer id;

    @Setter
    String title;

    @Setter
    String subtitle;

    @Setter
    @Column(columnDefinition = "TEXT")
    String content;

    @Setter
    LocalDate unlockDate;

    LocalDate dateOpened;

    // ===== Relations =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    User user;


    public boolean isLocked() {
        return unlockDate != null && LocalDate.now().isBefore(unlockDate);
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
        return getId() != null && Objects.equals(getId(), timeCapsule.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
