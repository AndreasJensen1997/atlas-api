package app.mappers;

import app.dtos.entityList.EntityListItemDTO;
import app.dtos.entityList.EntityListRequestDTO;
import app.dtos.entityList.EntityListResponseDTO;
import app.entities.EntityList;
import app.entities.EntityListItem;
import app.entities.User;

import java.util.Collections;
import java.util.List;

public class EntityListMapper {

    public EntityList toEntity(EntityListRequestDTO dto, User user) {
        EntityList list = EntityList.builder()
                .title(dto.title())
                .subtitle(dto.subtitle())
                .visibility(dto.visibility())
                .user(user)
                .build();

        // Convert incoming item strings into EntityListItem children using entity helper
        if (dto.items() != null) {
            for (String itemText : dto.items()) {
                EntityListItem item = EntityListItem.builder()
                        .text(itemText)
                        .build();
                list.addItem(item);
            }
        }

        return list;
    }

    public EntityListResponseDTO toResponseDTO(EntityList entity) {
        List<EntityListItemDTO> itemDTOs = entity.getItems() == null ? Collections.emptyList() :
                entity.getItems().stream()
                        .map(item -> new EntityListItemDTO(item.getItemId(), item.getText()))
                        .toList();

        return new EntityListResponseDTO(
                entity.getListId(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getItemAmount(),
                entity.getVisibility(),
                itemDTOs
        );
    }

}
