package app.config;

import app.controllers.*;
import app.daos.user.UserDAO;
import app.daos.userOwned.*;
import app.exceptions.ApiException;
import app.mappers.*;
import app.services.*;
import app.utils.security.SecurityFilter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import io.javalin.validation.ValidationException;


import java.util.Map;

@Slf4j
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
    EntityListController entityListController;
    UserController userController;

    UserService userService;

    public ApplicationConfig(EntityManagerFactory emf) {

        // ===== User =====

        UserDAO userDAO = new UserDAO(emf);
        userService = new UserService(userDAO);
        authController = new AuthController(userService);
        userController = new UserController(userService);

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

        // ===== EntityList =====
        EntityListDAO entityListDAO = new EntityListDAO(emf);
        EntityListMapper entityListMapper = new EntityListMapper();
        EntityListService entityListService = new EntityListService(entityListDAO, userService, entityListMapper);
        entityListController = new EntityListController(entityListService, entityListMapper);
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
        entityListController.addEndpoints();
        userController.addEndpoints();
    }


    public void configuration(JavalinConfig javalinConfig) {
        javalinConfig.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new JavaTimeModule());

        }));

        // ===== Define API Routes =====
        javalinConfig.router.apiBuilder(this);

    }

    public Javalin startServer(int port) {
        var app = Javalin.create(this::configuration);

        // ===== Security Filter =====
        app.before("/api/*", ctx -> SecurityFilter.verifyToken(ctx, userService));

        // ===== Global Exception Handling =====

        // 1. Javalin Validation Exception (400 Bad Request)
        // Triggered by ctx.bodyValidator(...) when validation rules fail
        app.exception(ValidationException.class, (e, ctx) -> {
            log.warn("Validation Error: {}", e.getErrors());
            ctx.status(400).json(Map.of(
                    "status", 400,
                    "error", e.getErrors()
            ));
        });

        // 2. Custom Business/API Exceptions (400, 401, 403, 404, etc.)
        // Thrown intentionally from services (e.g., throw new ApiException(404, "Chapter not found"))
        app.exception(ApiException.class, (e, ctx) -> {
            log.warn("API Exception ({}): {}", e.getStatusCode(), e.getMessage());
            ctx.status(e.getStatusCode()).json(Map.of(
                    "status", e.getStatusCode(),
                    "error", e.getMessage()
            ));
        });

        // 3. Bad Input / Argument Exception (400 Bad Request)
        // Thrown when invalid parameters or primitive conversions fail (e.g., Integer.parseInt)
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            log.warn("Illegal Argument: {}", e.getMessage());
            ctx.status(400).json(Map.of(
                    "status", 400,
                    "error", e.getMessage()
            ));
        });

        // 4. JPA Entity Not Found (404 Not Found)
        // Thrown by Hibernate/JPA if an entity is missing during lookup
        app.exception(jakarta.persistence.EntityNotFoundException.class, (e, ctx) -> {
            log.warn("Entity Not Found: {}", e.getMessage());
            ctx.status(404).json(Map.of(
                    "status", 404,
                    "error", e.getMessage()
            ));
        });

        // 5. Fallback Catch-All (500 Internal Server Error) - MUST BE LAST!
        // Catches unexpected crashes or database failure bugs
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unhandled Internal Server Error", e);
            ctx.status(500).json(Map.of(
                    "status", 500,
                    "error", "An unexpected internal server error occurred."
            ));
        });

        app.start(port);
        return app;
    }

    public void stopServer(Javalin app) {
        app.stop();
    }

}
