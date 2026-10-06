package app.services;

import app.daos.userOwned.ChapterDAO;
import app.dtos.chapter.ChapterRequestDTO;
import app.entities.Chapter;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.ChapterMapper;

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

    // ===== Create =====

    public Chapter createChapter(ChapterRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        if (chapterDAO.findByTitleAndUserId(dto.title(), owner.getId()) != null) {
            throw new IllegalArgumentException("A chapter with this title already exists.");
        }

        Chapter newChapter = chapterMapper.toEntity(dto, owner);

        return chapterDAO.create(newChapter);
    }

    // ===== Read =====

    public Chapter getById(Integer chapterId, int userId) {
        Chapter chapter = chapterDAO.getById(chapterId);

        if (chapter == null) {
            throw new IllegalArgumentException("Chapter not found with ID: " + chapterId);
        }

        if (!chapter.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Chapter not found with ID: " + chapterId);
        }
        return chapter;
    }

    public List<Chapter> getAllById(int userId) {

        return chapterDAO.getAllByUserId(userId);
    }

    public Chapter getRandom(Integer userId) {

        Chapter randomChapter = chapterDAO.getRandomByUserId(userId);

        if (randomChapter == null) {
            throw new IllegalArgumentException("No chapters were found");
        }

        if (!randomChapter.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this chapter.");
        }

        return randomChapter;
    }

    // ===== Update =====

    public Chapter update(Integer chapterId, ChapterRequestDTO dto, int userId) {
        Chapter chapter = chapterDAO.getById(chapterId);

        if (chapter == null) {
            throw new IllegalArgumentException("Chapter not found with ID: " + chapterId);
        }

        if (!chapter.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this chapter.");
        }

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
