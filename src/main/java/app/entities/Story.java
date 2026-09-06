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
public class Story implements LinkableEntity {

    @Id
    @GeneratedValue
    Integer storyId;
    String title;
    String subTitle;
    String content;
    LocalDate startDate;
    LocalDate endDate;

    // RELATIONS

    // M:1
    @ManyToOne
    @Setter
    Chapter chapter;





    @Override
    public Integer getId() {
        return storyId;
    }




}
