package app.services;

import app.daos.userOwned.ChapterDAO;
import app.dtos.chapter.ChapterRequestDTO;
import app.entities.Chapter;
import app.entities.User;
import app.mappers.ChapterMapper;

import java.util.List;


public class ChapterService {


    private final ChapterDAO chapterDAO;
    private final UserService userService;

    public ChapterService(ChapterDAO chapterDAO, UserService userService) {
        this.chapterDAO = chapterDAO;
        this.userService = userService;
    }

    public Chapter createChapter(ChapterRequestDTO dto, int userId) {
        User owner = userService.getById(userId);


        if (chapterDAO.findByTitleAndUserId(dto.title(), owner.getUserId()) != null) {
            throw new IllegalArgumentException("A chapter with this title already exists.");
        }

        Chapter newChapter = ChapterMapper.toEntity(dto, owner);

        return chapterDAO.create(newChapter);
    }


    public Chapter getById(Integer chapterId, int userId) {
        Chapter chapter = chapterDAO.getById(chapterId);

        if (chapter == null) {
            throw new IllegalArgumentException("Chapter not found with ID: " + chapterId);
        }

        if (!chapter.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this chapter.");
        }
        return chapter;
    }

    public List<Chapter> getAllById(int userId) {

        return chapterDAO.getAllByUserId(userId);
    }


    public void delete(Integer chapterId, int userId){
        Chapter chapter = getById(chapterId, userId);

        chapterDAO.delete(chapter.getId());
    }


    public Chapter update(Integer chapterId, ChapterRequestDTO dto, int userId) {
        Chapter chapter = chapterDAO.getById(chapterId);

        if (chapter == null) {
            throw new IllegalArgumentException("Chapter not found with ID: " + chapterId);
        }

        if (!chapter.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this chapter.");
        }

        chapter.setTitle(dto.title());
        chapter.setSubtitle(dto.subtitle());
        chapter.setContent(dto.content());
        chapter.setVisibility(dto.visibility());


        return chapterDAO.update(chapter);
    }







}
