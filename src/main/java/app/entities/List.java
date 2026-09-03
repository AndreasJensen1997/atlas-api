package app.entities;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
public class List {


    @Id
    @GeneratedValue
    Integer listId;



    @ManyToOne
    @JoinColumn(name = "user_id") // Explicitly names the foreign key column in the artifact table
    @Setter
    AppUser appUser;
}
