package app.services;

import app.daos.userOwned.ChapterDAO;
import app.dtos.chapter.ChapterRequestDTO;
import app.entities.Chapter;
import app.entities.User;
import app.mappers.ChapterMapper;


public class ChapterService {


    private final ChapterDAO chapterDAO;
    private final UserService userService;

    public ChapterService(ChapterDAO chapterDAO, UserService userService) {
        this.chapterDAO = chapterDAO;
        this.userService = userService;
    }

    public Chapter createChapter(ChapterRequestDTO dto, int userId) {

        if (chapterDAO.findByTitle(dto.title()) != null) {
            throw new IllegalArgumentException("A chapter with this title already exists.");
        }

        User owner = userService.getById(userId);

        Chapter newChapter = ChapterMapper.toEntity(dto, owner);

        return chapterDAO.create(newChapter);
    }
}
