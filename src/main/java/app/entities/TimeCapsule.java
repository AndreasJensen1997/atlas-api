package app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    @Setter
    AppUser appUser;

}
