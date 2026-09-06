package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Place implements LinkableEntity {


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



    @Override
    public Integer getId() {
        return placeId;
    }


}
