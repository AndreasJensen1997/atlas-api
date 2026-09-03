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
public class Story {

    @Id
    @GeneratedValue
    Integer storyId;
    String title;
    String subTitle;
    LocalDate startDate;
    LocalDate endDate;

    // RELATIONS

    // M:1
    @ManyToOne
    @Setter
    Chapter chapter;


    // 1:M
    @OneToMany(mappedBy = "mention")
    @Setter
   private Set<Mention> mentions = new HashSet<>();





}
