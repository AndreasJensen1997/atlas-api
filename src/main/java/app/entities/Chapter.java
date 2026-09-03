package app.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Chapter {


    @Id
    @GeneratedValue
    Integer chapterId;
    String title;
    String subtitle;
    LocalDate startDate;
    LocalDate endDate;


    // ===== RELATIONS =====

    // M:1
    @ManyToOne
    @Setter
    AppUser appUser;


    // 1:M
    @OneToMany(mappedBy = "chapter")
    private Set<Story> stories = new HashSet<>();

}
