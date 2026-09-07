package app.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Fragment implements LinkableEntity {
    @Id
    @GeneratedValue
    Integer fragmentId;
    private String title;
    private String subtitle;
    String content;
    LocalDate createdAt;
    LocalDate updatedAt;
    int wordCount;


    // ===== RELATIONS =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    AppUser appUser;


    @Override
    public Integer getId() {
        return fragmentId;
    }

    // ===== JPA LIFECYCLE CALLBACKS =====

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        updatedAt = LocalDate.now();
        calculateWordCount();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
        calculateWordCount();
    }

    private void calculateWordCount() {
        if (content == null || content.trim().isEmpty()) {
            this.wordCount = 0;
        } else {
            this.wordCount = content.trim().split("\\s+").length;
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
        Fragment fragment = (Fragment) o;
        return getFragmentId() != null && Objects.equals(getFragmentId(), fragment.getFragmentId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }


}
