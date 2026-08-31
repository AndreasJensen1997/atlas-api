package app.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Chapter {


    @Id
    @GeneratedValue
    int chapterId;
    int userId;
    String title;
    String subtitle;
    LocalDate startDate;
    LocalDate endDate;


    // RELATIONS

    // M:1
    @ManyToOne(cascade = CascadeType.ALL)
    @Setter
    AppUser appUser;

}
