package app.entities;

import app.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
}