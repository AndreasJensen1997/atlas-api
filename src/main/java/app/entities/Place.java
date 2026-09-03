package app.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Place {


    @Id
    @GeneratedValue
    Integer placeId;
    private String name;
    // x-cordinate
    // y-cordinate



    // RELATIONS

    // M:1
    @ManyToOne
    @JoinColumn(name = "user_id")
    @Setter
    AppUser appUser;

}
