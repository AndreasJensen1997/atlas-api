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
    Integer fragmentId;
    String title;
    String subTitle;
    LocalDate createdAt;


    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    AppUser appUser;
}
