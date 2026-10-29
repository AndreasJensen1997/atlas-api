package app.services;

import app.daos.userOwned.ChapterDAO;
import app.dtos.chapter.ChapterRequestDTO;
import app.entities.Chapter;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.ChapterMapper;
import io.javalin.http.HttpStatus;


import java.util.List;


public class ChapterService {

    // ===== Dependencies =====

    private final ChapterDAO chapterDAO;
    private final UserService userService;
    private final ChapterMapper chapterMapper;

    // ===== Constructor =====

    public ChapterService(ChapterDAO chapterDAO, UserService userService, ChapterMapper chapterMapper) {
        this.chapterDAO = chapterDAO;
        this.userService = userService;
        this.chapterMapper = chapterMapper;
    }


    public Chapter createChapter(ChapterRequestDTO dto, int userId) {
        User owner = userService.getById(userId);
        Chapter newChapter = chapterMapper.toEntity(dto, owner);
        return chapterDAO.create(newChapter);
    }

    // ===== Read =====

    public Chapter getById(Integer chapterId, int userId) {
        Chapter chapter = chapterDAO.getById(chapterId);

        if (!chapter.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Chapter not found with ID: " + chapterId);
        }
        return chapter;
    }

    public List<Chapter> getAllById(int userId) {

        return chapterDAO.getAllByUserId(userId);
    }

    public Chapter getRandom(Integer userId) {
        Chapter randomChapter = chapterDAO.getRandomByUserId(userId);

        if (randomChapter == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No chapters were found");
        }

        return randomChapter;
    }

    // ===== Update =====

    public Chapter update(Integer chapterId, ChapterRequestDTO dto, int userId) {
        Chapter chapter = getById(chapterId, userId);

        chapter.setTitle(dto.title());
        chapter.setSubtitle(dto.subtitle());
        chapter.setContent(dto.content());
        chapter.setStartDate(dto.startDate());
        chapter.setEndDate(dto.endDate());
        chapter.setVisibility(dto.visibility());

        return chapterDAO.update(chapter);
    }

    // ===== Delete =====

    public void delete(Integer chapterId, int userId) {
        Chapter chapter = getById(chapterId, userId);

        chapterDAO.delete(chapter.getId());
    }
}
