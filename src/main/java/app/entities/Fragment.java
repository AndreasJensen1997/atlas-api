package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Fragment {
    @Id
    @GeneratedValue
    Integer fragmentId;
    private String title;
    private String subTitle;
    String content;
    LocalDate createdAt;
    int wordCount;


    // RELATIONS

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    AppUser appUser;

}
