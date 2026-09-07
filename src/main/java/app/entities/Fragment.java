package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
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
    private String subTitle;
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
        createdAt = LocalDate.now();
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


}
