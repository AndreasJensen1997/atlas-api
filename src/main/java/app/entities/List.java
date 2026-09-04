package app.entities;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
public class List {


    @Id
    @GeneratedValue
    Integer listId;



    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    AppUser appUser;
}
