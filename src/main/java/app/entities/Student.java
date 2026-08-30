package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDateTime createdAt;
    private String email;
    private String name;
    private LocalDateTime updatedAt;


    // RELATIONS

    // 1:M
    @ManyToOne(cascade = CascadeType.ALL)
    @Setter
    Course course;


}
