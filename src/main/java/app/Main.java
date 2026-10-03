package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.daos.user.UserDAO;
import app.exceptions.ApiException;
import app.services.UserService;
import app.utils.security.SecurityFilter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Initializing Atlas Application...");

        // ===== Initialize Database & Services =====

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        UserDAO userDAO = new UserDAO(emf);
        UserService userService = new UserService(userDAO);

        ApplicationConfig applicationConfig = new ApplicationConfig(emf);

        // ===== Create Javalin Instance =====

        Javalin app = Javalin.create(config -> {

            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.registerModule(new JavaTimeModule());
            }));

            // ===== Define API Routes =====
            config.router.apiBuilder(applicationConfig::addEndpoints);
        });

        // ===== Security Filter =====
        // Runs specifically on /api/* routes before hitting controllers
        app.before("/api/*", ctx -> SecurityFilter.verifyToken(ctx, userService));

        // ===== Global Exception Handling =====

        // Converts throw new ApiException(code, message) to clean JSON response
        app.exception(ApiException.class, (e, ctx) -> {
            logger.warn("API Exception ({}): {}", e.getStatusCode(), e.getMessage());
            ctx.status(e.getStatusCode()).json(Map.of(
                    "status", e.getStatusCode(),
                    "error", e.getMessage()
            ));
        });

        // Converts throw new IllegalArgumentException(message) to 400 Bad Request
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            logger.warn("Validation Error: {}", e.getMessage());
            ctx.status(400).json(Map.of(
                    "status", 400,
                    "error", e.getMessage()
            ));
        });

        // Fallback for unhandled server errors
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Internal Server Error", e);
            ctx.status(500).json(Map.of(
                    "status", 500,
                    "error", "An unexpected internal server error occurred."
            ));
        });

        // ===== Start Server =====

        app.start(7070);
        System.out.println("Atlas API is running on http://localhost:7070");
    }
}