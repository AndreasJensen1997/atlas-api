package app.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class EntityListItem {

    @Id
    @GeneratedValue
    private Integer itemId;
    private String text;

    // RELATIONS

    // M:1
    @ManyToOne(optional = false)
    @JoinColumn(name = "entity_list_id", nullable = false)
    private EntityList entityList;
}

