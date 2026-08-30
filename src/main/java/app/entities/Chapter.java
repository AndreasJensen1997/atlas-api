package app.entities;


import jakarta.persistence.*;
import lombok.*;

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
    String title;
    String subtitle;
    Date startDate;
    Date endDate;


    // RELATIONS

    // M:1
    @ManyToOne(cascade = CascadeType.ALL)
    @Setter
    AppUser appUser;

}
