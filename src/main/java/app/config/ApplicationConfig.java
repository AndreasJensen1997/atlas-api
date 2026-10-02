package app.config;

import app.controllers.*;
import app.daos.user.UserDAO;
import app.daos.userOwned.*;
import app.mappers.ArtifactMapper;
import app.mappers.ChapterMapper;
import app.mappers.MemoryMapper;
import app.mappers.StoryMapper;
import app.services.*;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

public class ApplicationConfig implements EndpointGroup {


    AuthController authController;
    GeminiPromptController geminiPromptController;
    ChapterController chapterController;
    MemoryController memoryController;
    StoryController storyController;
    ArtifactController artifactController;

    public ApplicationConfig(EntityManagerFactory emf) {


        // USER
        UserDAO userDAO = new UserDAO(emf);
        UserService userService = new UserService(userDAO);
        authController = new AuthController(userService);

        // GEMINI PROMPT
        GeminiPromptDAO geminiPromptDAO = new GeminiPromptDAO(emf);
        GeminiPromptService geminiPromptService = new GeminiPromptService(geminiPromptDAO, userService);
        geminiPromptController = new GeminiPromptController(geminiPromptService);

        // CHAPTER
        ChapterDAO chapterDAO = new ChapterDAO(emf);
        ChapterMapper chapterMapper = new ChapterMapper();
        ChapterService chapterService = new ChapterService(chapterDAO, userService, chapterMapper);
        chapterController = new ChapterController(chapterService, chapterMapper);

        // STORY
        StoryDAO storyDAO = new StoryDAO(emf);
        StoryMapper storyMapper = new StoryMapper();
        StoryService storyService = new StoryService(storyDAO, userService, storyMapper);
        storyController = new StoryController(storyService, storyMapper);

        // MEMORY
        MemoryDAO memoryDAO = new MemoryDAO(emf);
        MemoryMapper memoryMapper = new MemoryMapper();
        MemoryService memoryService = new MemoryService(memoryDAO, userService, memoryMapper);
        memoryController = new MemoryController(memoryService, memoryMapper);

        // ARTIFACT TYPE
        ArtifactTypeDAO artifactTypeDAO = new ArtifactTypeDAO(emf);
        ArtifactTypeService artifactTypeService = new ArtifactTypeService(artifactTypeDAO);

        // ARTIFACT
        ArtifactDAO artifactDAO = new ArtifactDAO(emf);
        ArtifactMapper artifactMapper = new ArtifactMapper();
        ArtifactService artifactService = new ArtifactService(artifactDAO, userService, artifactTypeService, artifactMapper);
        artifactController = new ArtifactController(artifactService, artifactMapper);

    }


    @Override
    public void addEndpoints() {
        authController.addEndpoints();
        geminiPromptController.addEndpoints();
        chapterController.addEndpoints();
        memoryController.addEndpoints();
        storyController.addEndpoints();
        artifactController.addEndpoints();
    }
}
