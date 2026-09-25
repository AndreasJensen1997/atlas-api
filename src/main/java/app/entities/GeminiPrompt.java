package app.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class GeminiPrompt {

    @Id
    @GeneratedValue
    private Integer geminiPromptId;
    String content;




    // ===== RELATIONS =====

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

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
        GeminiPrompt geminiPrompt = (GeminiPrompt) o;
        return getGeminiPromptId() != null && Objects.equals(getGeminiPromptId(), geminiPrompt.getGeminiPromptId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }
}
