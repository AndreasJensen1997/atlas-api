package app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class List {


    @Id
    @GeneratedValue
    Integer listId;
}
