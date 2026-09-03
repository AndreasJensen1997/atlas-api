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
public class Memory {


    @Id
    @GeneratedValue
    Integer memory_id;
    String title;
    String subTitle;
    LocalDate date;


    // RELATIONS

    // M:1
    @ManyToOne
    @Setter
    Chapter chapter;

    @ManyToOne
    @Setter
    Chapter Story;


    // 1:M
    @OneToMany (mappedBy = "memory")
    @Setter
    private Set<Mention> mentions = new HashSet<>();



}
