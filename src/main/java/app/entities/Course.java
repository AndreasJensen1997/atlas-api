package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String description;
    @Enumerated(  EnumType.STRING)
    private CourseName courseName;
    private LocalDate startDate;
    private LocalDate endDate;


    // RELATIONS

    // 1:M
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @Builder.Default
    Set<Student> students = new HashSet<>();


    public void addStudent(Student student) {
        this.students.add(student);
        if (student != null) {
            student.setCourse(this);
        }
    }

    // M:1

    @ManyToOne()
    @Setter
    Teacher teacher;


}
