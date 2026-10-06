package app.services;

import app.daos.userOwned.EntityListDAO;
import app.dtos.entityList.EntityListRequestDTO;
import app.entities.EntityList;
import app.entities.EntityListItem;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.EntityListMapper;

import java.util.List;

public class EntityListService {

    // ===== Dependencies =====

    EntityListDAO entityListDAO;
    UserService userService;
    EntityListMapper entityListMapper;

    // ===== Constructor =====

    public EntityListService(EntityListDAO entityListDAO, UserService userService, EntityListMapper entityListMapper) {
        this.entityListDAO = entityListDAO;
        this.userService = userService;
        this.entityListMapper = entityListMapper;
    }

    // ===== Create =====

    public EntityList createEntityList(EntityListRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        if (entityListDAO.findByTitleAndUserId(dto.title(), owner.getId()) != null) {
            throw new IllegalArgumentException("A Entity list with this title already exists.");
        }

        EntityList newEntityList = entityListMapper.toEntity(dto, owner);

        return entityListDAO.create(newEntityList);
    }

    // ===== Read =====

    public EntityList getById(Integer entityListId, int userId) {
        EntityList entityList = entityListDAO.getById(entityListId);

        if (entityList == null) {
            throw new IllegalArgumentException("List not found with ID: " + entityListId);
        }

        if (!entityList.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Chapter not found with ID: " + entityList);
        }
        return entityList;
    }

    public List<EntityList> getAllById(int userId) {

        return entityListDAO.getAllByUserId(userId);
    }

    public EntityList getRandom(Integer userId) {

        EntityList randomEntityList = entityListDAO.getRandomByUserId(userId);

        if (randomEntityList == null) {
            throw new IllegalArgumentException("No lists were found");
        }

        if (!randomEntityList.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this list.");
        }

        return randomEntityList;
    }

    // ===== Update =====

    public EntityList update(Integer entityListId, EntityListRequestDTO dto, int userId) {
        EntityList entityList = getById(entityListId, userId); // Handles ownership & non-null checks

        entityList.setTitle(dto.title());
        entityList.setSubtitle(dto.subtitle());
        entityList.setVisibility(dto.visibility());

        if (dto.items() != null) {
            entityList.clearAllItems();

            for (String itemText : dto.items()) {
                EntityListItem newItem = EntityListItem.builder()
                        .text(itemText)
                        .build();
                entityList.addItem(newItem);
            }
        }

        return entityListDAO.update(entityList);
    }

    public EntityList addItemToList(Integer entityListId, String itemText, int userId){
        EntityList entityList = getById(entityListId, userId);

        EntityListItem item = EntityListItem.builder()
                .text(itemText)
                .build();

        entityList.addItem(item);
        return entityListDAO.update(entityList);
    }

    public EntityList removeItemFromList(Integer entityListId, Integer itemId, int userId){
        EntityList entityList = getById(entityListId, userId);

        EntityListItem targetItem = entityList.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in this list with ID: " + itemId));

        entityList.removeItem(targetItem);
        return entityListDAO.update(entityList);

    }

    // ===== Delete =====

    public void delete(Integer entityListId, int userId) {
        EntityList entityList = getById(entityListId, userId);

        entityListDAO.delete(entityList.getId());
    }
}
