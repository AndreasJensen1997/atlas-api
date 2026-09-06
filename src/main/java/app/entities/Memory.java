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

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private AppUser appUser;


    @Override
    public Integer getId() {
        return memoryId;
    }


}
