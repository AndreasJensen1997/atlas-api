package app;

import app.config.HibernateConfig;
import app.controllers.GeminiPromptController;
import app.controllers.UserController;
import app.daos.AppUserDAO;

import app.daos.userOwned.GeminiPromptDAO;
import app.services.AppUserService;
import app.services.GeminiPromptService;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Application is running successfully");

        // 1. Initialize Database
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        AppUserDAO appUserDAO = new AppUserDAO(emf);
        AppUserService appUserService = new AppUserService(appUserDAO);
        UserController userController = new UserController(appUserService);

        GeminiPromptDAO geminiPromptDAO = new GeminiPromptDAO(emf);
        GeminiPromptService geminiPromptService = new GeminiPromptService(geminiPromptDAO);
        GeminiPromptController geminiPromptController = new GeminiPromptController(geminiPromptService);





        // 2. Start Javalin Server
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(7070);




        // 3. Define API Routes
        app.post("/api/register", userController::register);
        app.post("/api/login", userController::login);
        app.post("/api/generatePrompt", geminiPromptController::generatePrompt);
        app.post("/api/savePrompt", geminiPromptController::savePrompt);
        System.out.println("Atlas API is running on http://localhost:7070");


    }


}
