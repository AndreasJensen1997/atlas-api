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
    String content;
    // x-cordinate
    // y-cordinate



    // RELATIONS

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    AppUser appUser;

}
