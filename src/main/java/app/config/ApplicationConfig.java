package app.config;

import app.controllers.AuthController;
import app.controllers.GeminiPromptController;
import app.daos.UserDAO;
import app.daos.userOwned.GeminiPromptDAO;
import app.services.GeminiPromptService;
import app.services.UserService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

public class ApplicationConfig implements EndpointGroup {


    AuthController authController;
    GeminiPromptController geminiPromptController;

    public ApplicationConfig(EntityManagerFactory emf) {

        UserDAO userDAO = new UserDAO(emf);
        GeminiPromptDAO geminiPromptDAO = new GeminiPromptDAO(emf);


        UserService userService = new UserService(userDAO);
        GeminiPromptService geminiPromptService = new GeminiPromptService(geminiPromptDAO,userService);
        authController = new AuthController(userService);
        geminiPromptController = new GeminiPromptController(geminiPromptService);


    }


    @Override
    public void addEndpoints() {
        authController.addEndpoints();
        geminiPromptController.addEndpoints();
    }
}
