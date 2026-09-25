package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.controllers.AuthController;
import app.controllers.GeminiPromptController;
import app.controllers.UserController;
import app.daos.UserDAO;

import app.daos.userOwned.GeminiPromptDAO;
import app.services.UserService;
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
        UserDAO userDAO = new UserDAO(emf);
        UserService userService = new UserService(userDAO);
        UserController userController = new UserController(userService);
        AuthController authController = new AuthController(userService);

        GeminiPromptDAO geminiPromptDAO = new GeminiPromptDAO(emf);
        GeminiPromptService geminiPromptService = new GeminiPromptService(geminiPromptDAO);
        GeminiPromptController geminiPromptController = new GeminiPromptController(geminiPromptService);
        ApplicationConfig applicationConfig = new ApplicationConfig(emf);



        // 2. Start Javalin Server
        Javalin app = Javalin.create(config -> {
            config.router.apiBuilder(applicationConfig::addEndpoints);
                }).start(7070);


        // 3. Define API Routes
        //userController.registerRoutes(app);
        geminiPromptController.registerRoutes(app);

        System.out.println("Atlas API is running on http://localhost:7070");


    }


}
