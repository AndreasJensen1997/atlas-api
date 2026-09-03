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
public class Artifact {


    @Id
    @GeneratedValue
    Integer fragment_id;
    String title;
    String subTitle;
    LocalDate createdAt;


    @ManyToOne
    @Setter
    AppUser appUser;

}
