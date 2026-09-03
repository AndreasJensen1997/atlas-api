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
public class Person {


    @Id
    @GeneratedValue
    Integer personId;
    String name;
    String relation;

    // RELATIONS

    // M:1
    @ManyToOne
    @Setter
    AppUser appUser;

}
