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
public class Memory implements LinkableEntity {


    @Id
    @GeneratedValue
    Integer memoryId;
    String title;
    String subTitle;
    String content;
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
    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Mention> mentions = new HashSet<>();


    @Override
    public Integer getId() {
        return memoryId;
    }


}
