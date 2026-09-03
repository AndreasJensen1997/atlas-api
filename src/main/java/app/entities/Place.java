package app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    @Setter
    AppUser appUser;

}
