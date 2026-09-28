package app.mappers;


import app.dtos.chapter.ChapterRequestDTO;
import app.dtos.chapter.ChapterResponseDTO;
import app.entities.Chapter;
import app.entities.User;

public class ChapterMapper {

    public static Chapter toEntity(ChapterRequestDTO dto, User user) {
        if (dto == null) return null;
        return Chapter.builder()
                .title(dto.title())
                .subtitle(dto.subtitle())
                .content(dto.content())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .visibility(dto.visibility())
                .user(user)
                .build();

    }


    public static ChapterResponseDTO toResponseDTO(Chapter chapter) {
        if (chapter == null) return null;

        return new ChapterResponseDTO(
                chapter.getChapterId(),
                chapter.getTitle(),
                chapter.getSubtitle(),
                chapter.getContent(),
                chapter.getStartDate(),
                chapter.getEndDate(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt(),
                chapter.getVisibility()

        );
    }

}
