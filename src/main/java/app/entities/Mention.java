package app.entities;

import app.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class Mention {

    // ===== Fields =====

    @Id
    @GeneratedValue
    private Integer id;

    // ===== The Owner (Where the highlighted text lives) =====

    @Column(nullable = false)
    private Integer ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType ownerType; // PERSON, PLACE, CHAPTER, ARTIFACT

    // ===== Text Positioning Tracking =====

    @Column(nullable = false)
    private Integer startIndex;

    @Column(nullable = false)
    private Integer endIndex;

    private String selectedText;

    // ===== The Target (What the link points to) =====

    @Column(nullable = false)
    private Integer targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType; // ARTIFACT, MEMORY, PERSON

    // ===== Relations =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

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
        Mention mention = (Mention) o;
        return getId() != null && Objects.equals(getId(), mention.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}