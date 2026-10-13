package app.services;

import app.daos.userOwned.StoryDAO;
import app.dtos.story.StoryRequestDTO;
import app.entities.Story;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.StoryMapper;

import java.util.List;

public class StoryService {

    // ===== Dependencies =====

    private final StoryDAO storyDAO;
    private final UserService userService;
    private final StoryMapper storyMapper;

    // ===== Constructor =====

    public StoryService(StoryDAO storyDAO, UserService userService, StoryMapper storyMapper) {
        this.storyDAO = storyDAO;
        this.userService = userService;
        this.storyMapper = storyMapper;
    }

    // ===== Create =====

    public Story createStory(StoryRequestDTO dto, int userId) {
        User owner = userService.getById(userId);
        Story newStory = storyMapper.toEntity(dto, owner);
        return storyDAO.create(newStory);
    }

    // ===== Read =====

    public Story getById(Integer storyId, int userId) {
        Story story = storyDAO.getById(storyId);

        if (!story.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Story not found with ID: " + storyId);
        }
        return story;
    }

    public List<Story> getAllById(int userId) {
        return storyDAO.getAllByUserId(userId);
    }

    public Story getRandom(Integer userId) {
        Story randomStory = storyDAO.getRandomByUserId(userId);

        if (randomStory == null) {
            throw new ApiException(404, "No stories were found");
        }

        return randomStory;
    }

    // ===== Update =====

    public Story update(Integer storyId, StoryRequestDTO dto, int userId) {
        Story story = getById(storyId, userId);

        story.setTitle(dto.title());
        story.setSubtitle(dto.subtitle());
        story.setContent(dto.content());
        story.setStartDate(dto.startDate());
        story.setEndDate(dto.endDate());
        story.setVisibility(dto.visibility());

        return storyDAO.update(story);
    }

    // ===== Delete =====

    public void delete(Integer storyId, int userId) {
        Story story = getById(storyId, userId);
        storyDAO.delete(story.getId());
    }
}