package app.entities;


import jakarta.persistence.*;
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
    String content;
    String relation;

    // RELATIONS

    // M:1
    @ManyToOne
    @JoinColumn(name = "user_id")
    @Setter
    AppUser appUser;

}
