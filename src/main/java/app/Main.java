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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Slf4j
public class Main {

    public static void main(String[] args) {

        log.info("Initializing Atlas Application...");

        // ===== Initialize Database & Services =====

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        ApplicationConfig app = new ApplicationConfig(emf);

        // ===== Start Server =====

        app.startServer(7070);
        System.out.println("Atlas API is running on http://localhost:7070");
    }
}