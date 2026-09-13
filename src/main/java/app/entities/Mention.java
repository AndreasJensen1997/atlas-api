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

    @Id
    @GeneratedValue
    private Integer mentionId;

    // ===== 1. THE OWNER (Where the highlighted text lives) =====
    @Column(nullable = false)
    private Integer ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType ownerType; // e.g., PERSON, PLACE, CHAPTER, ARTIFACT

    // Text position tracking
    private Integer startIndex;
    private Integer endIndex;
    private String selectedText;

    // ===== 2. THE TARGET (What the link points to) =====
    @Column(nullable = false)
    private Integer targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType; // e.g., TRIP, MEMORY, PERSON





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
        Mention mention = (Mention) o;
        return getMentionId() != null && Objects.equals(getMentionId(), mention.getMentionId());
    }


    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }


}