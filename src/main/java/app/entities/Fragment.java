package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class Fragment implements LinkableEntity {
    @Id
    @GeneratedValue
    Integer fragmentId;
    private String title;
    private String subTitle;
    String content;
    LocalDate createdAt;
    int wordCount;


    // RELATIONS

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    AppUser appUser;

    // 1:M
    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Mention> mentions = new HashSet<>();



    @Override
    public Integer getId() {
        return fragmentId;
    }


}
