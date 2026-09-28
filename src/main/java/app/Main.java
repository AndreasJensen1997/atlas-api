package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.daos.user.UserDAO;

import app.services.UserService;
import app.utils.security.SecurityFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
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

        ApplicationConfig applicationConfig = new ApplicationConfig(emf);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());


        // 2. Start Javalin Server
        // 3. Define API Routes
        Javalin app = Javalin.create(config -> {

            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.registerModule(new JavaTimeModule());
            }));

            config.router.apiBuilder(applicationConfig::addEndpoints);
        }).start(7070);

        app.before(ctx -> SecurityFilter.verifyToken(ctx, userService));

        System.out.println("Atlas API is running on http://localhost:7070");

    }


}
