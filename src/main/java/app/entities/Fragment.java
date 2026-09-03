package app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    LocalDate createdAt;
    int wordCount;


    // RELATIONS

    // M:1
    @ManyToOne
    @Setter
    AppUser appUser;

}
