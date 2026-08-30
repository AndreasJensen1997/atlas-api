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
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String name;
    private String email;
    private String password;



    // RELATIONS

    // 1:M
    @OneToMany(cascade = CascadeType.ALL)
    @Setter
    private Set<Chapter> chapters = new HashSet<>();

}
