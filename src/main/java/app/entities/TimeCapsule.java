package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class TimeCapsule {

    @Id
    @GeneratedValue
    Integer timeCapsuleId;
    String content;
    Date unlockDate;
    Date dateOpened;
    boolean lockStatus;


    // RELATIONS

    // M:1
    @ManyToOne
    @JoinColumn(name = "user_id")
    @Setter
    AppUser appUser;

}
