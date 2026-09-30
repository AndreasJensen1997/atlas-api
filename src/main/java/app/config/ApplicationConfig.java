package app.config;

import app.controllers.AuthController;
import app.controllers.ChapterController;
import app.controllers.GeminiPromptController;
import app.daos.user.UserDAO;
import app.daos.userOwned.ChapterDAO;
import app.daos.userOwned.GeminiPromptDAO;
import app.mappers.ChapterMapper;
import app.services.ChapterService;
import app.services.GeminiPromptService;
import app.services.UserService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

public class ApplicationConfig implements EndpointGroup {


    AuthController authController;
    GeminiPromptController geminiPromptController;
    ChapterController chapterController;

    public ApplicationConfig(EntityManagerFactory emf) {


        // USER
        UserDAO userDAO = new UserDAO(emf);
        UserService userService = new UserService(userDAO);
        authController = new AuthController(userService);


        // GEMINI PROMPT
        GeminiPromptDAO geminiPromptDAO = new GeminiPromptDAO(emf);
        GeminiPromptService geminiPromptService = new GeminiPromptService(geminiPromptDAO,userService);
        geminiPromptController = new GeminiPromptController(geminiPromptService);

        // CHAPTER
        ChapterDAO chapterDAO = new ChapterDAO(emf);
        ChapterMapper chapterMapper = new ChapterMapper();
        ChapterService chapterService = new ChapterService(chapterDAO,userService, chapterMapper);
        chapterController = new ChapterController(chapterService, chapterMapper);

    }


    @Override
    public void addEndpoints() {
        authController.addEndpoints();
        geminiPromptController.addEndpoints();
        chapterController.addEndpoints();
    }
}
