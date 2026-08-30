package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString

public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private String name;
    private String zoom;


    // RELATIONS

    // 1:M
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    public Set<Course> courses = new HashSet<>();

    public void addCourse(Course course) {
        this.courses.add(course);
        if (course != null) {
            course.setTeacher(this);
        }
    }


}
