package app.config;

import app.controllers.AuthController;
import app.daos.UserDAO;
import app.services.UserService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

public class ApplicationConfig implements EndpointGroup {


    AuthController authController;

    public ApplicationConfig(EntityManagerFactory emf) {

        UserDAO userDAO = new UserDAO(emf);


        UserService userService = new UserService(userDAO);
        authController = new AuthController(userService);


    }


    @Override
    public void addEndpoints() {
        authController.addEndpoints();

    }
}
