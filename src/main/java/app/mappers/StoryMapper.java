package app.mappers;

import app.dtos.story.StoryRequestDTO;
import app.dtos.story.StoryResponseDTO;
import app.entities.Story;
import app.entities.User;
import app.mappers.generics.IMapper;

public class StoryMapper implements IMapper<StoryRequestDTO, StoryResponseDTO, Story, User> {

    @Override
    public Story toEntity(StoryRequestDTO dto, User user) {
        if (dto == null) return null;
        return Story.builder()
                .title(dto.title())
                .subtitle(dto.subtitle())
                .content(dto.content())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .visibility(dto.visibility())
                .user(user)
                .build();

    }

    @Override
    public StoryResponseDTO toResponse(Story story) {
        if (story == null) return null;

        return new StoryResponseDTO(
                story.getId(),
                story.getTitle(),
                story.getSubtitle(),
                story.getContent(),
                story.getStartDate(),
                story.getEndDate(),
                story.getCreatedAt(),
                story.getUpdatedAt(),
                story.getVisibility()

        );
    }
}