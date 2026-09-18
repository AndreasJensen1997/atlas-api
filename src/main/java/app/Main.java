package app;

import app.config.HibernateConfig;
import app.controllers.UserController;
import app.daos.AppUserDAO;

import app.dtos.UserLoginDTO;
import app.dtos.UserRegistrationDTO;
import app.services.UserService;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static void main(String[] args) {

        // 1. Initialize Database
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        AppUserDAO appUserDAO = new AppUserDAO(emf);
        UserService userService = new UserService(appUserDAO);
        UserController userController = new UserController(userService);

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

        System.out.println("Atlas API is running on http://localhost:7070");


    }


}
