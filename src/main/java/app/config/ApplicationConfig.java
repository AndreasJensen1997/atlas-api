package app.config;

import app.controllers.*;
import app.daos.user.UserDAO;
import app.daos.userOwned.*;
import app.mappers.*;
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
    PersonController personController;
    PlaceController placeController;
    TimeCapsuleController timeCapsuleController;
    FragmentController fragmentController;
    MentionController mentionController;

    public ApplicationConfig(EntityManagerFactory emf) {

        // ===== User =====

        UserDAO userDAO = new UserDAO(emf);
        UserService userService = new UserService(userDAO);
        authController = new AuthController(userService);

        // ===== Gemini prompt =====

        GeminiPromptDAO geminiPromptDAO = new GeminiPromptDAO(emf);
        GeminiPromptService geminiPromptService = new GeminiPromptService(geminiPromptDAO, userService);
        geminiPromptController = new GeminiPromptController(geminiPromptService);

        // ===== Chapter =====

        ChapterDAO chapterDAO = new ChapterDAO(emf);
        ChapterMapper chapterMapper = new ChapterMapper();
        ChapterService chapterService = new ChapterService(chapterDAO, userService, chapterMapper);
        chapterController = new ChapterController(chapterService, chapterMapper);

        // ===== Story =====

        StoryDAO storyDAO = new StoryDAO(emf);
        StoryMapper storyMapper = new StoryMapper();
        StoryService storyService = new StoryService(storyDAO, userService, storyMapper);
        storyController = new StoryController(storyService, storyMapper);

        // ===== Memory =====

        MemoryDAO memoryDAO = new MemoryDAO(emf);
        MemoryMapper memoryMapper = new MemoryMapper();
        MemoryService memoryService = new MemoryService(memoryDAO, userService, memoryMapper);
        memoryController = new MemoryController(memoryService, memoryMapper);

        // ===== Artifact type =====

        ArtifactTypeDAO artifactTypeDAO = new ArtifactTypeDAO(emf);
        ArtifactTypeService artifactTypeService = new ArtifactTypeService(artifactTypeDAO);

        // ===== Artifact =====

        ArtifactDAO artifactDAO = new ArtifactDAO(emf);
        ArtifactMapper artifactMapper = new ArtifactMapper();
        ArtifactService artifactService = new ArtifactService(artifactDAO, userService, artifactTypeService, artifactMapper);
        artifactController = new ArtifactController(artifactService, artifactMapper);

        // ===== Person =====

        PersonDAO personDAO = new PersonDAO(emf);
        PersonMapper personMapper = new PersonMapper();
        PersonService personService = new PersonService(personDAO, userService, personMapper);
        personController = new PersonController(personService, personMapper);

        // ===== Place =====

        PlaceDAO placeDAO = new PlaceDAO(emf);
        PlaceMapper placeMapper = new PlaceMapper();
        PlaceService placeService = new PlaceService(placeDAO, userService, placeMapper);
        placeController = new PlaceController(placeService, placeMapper);

        // ===== Time capsule =====

        TimeCapsuleDAO timeCapsuleDAO = new TimeCapsuleDAO(emf);
        TimeCapsuleMapper timeCapsuleMapper = new TimeCapsuleMapper();
        TimeCapsuleService timeCapsuleService = new TimeCapsuleService(timeCapsuleDAO, userService, timeCapsuleMapper);
        timeCapsuleController = new TimeCapsuleController(timeCapsuleService, timeCapsuleMapper);

        // ===== Fragment =====

        FragmentDAO fragmentDAO = new FragmentDAO(emf);
        FragmentMapper fragmentMapper = new FragmentMapper();
        FragmentService fragmentService = new FragmentService(fragmentDAO, userService, fragmentMapper);
        fragmentController = new FragmentController(fragmentService, fragmentMapper);

        // ===== Mention =====

        MentionDAO mentionDAO = new MentionDAO(emf);
        MentionMapper mentionMapper = new MentionMapper();
        MentionService mentionService = new MentionService(mentionDAO, mentionMapper);
        mentionController = new MentionController(mentionService);
    }


    @Override
    public void addEndpoints() {
        authController.addEndpoints();
        geminiPromptController.addEndpoints();
        chapterController.addEndpoints();
        memoryController.addEndpoints();
        storyController.addEndpoints();
        artifactController.addEndpoints();
        personController.addEndpoints();
        placeController.addEndpoints();
        timeCapsuleController.addEndpoints();
        fragmentController.addEndpoints();
        mentionController.addEndpoints();
    }
}
