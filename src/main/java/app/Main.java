package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.utils.scheduling.NotificationScheduler;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;


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

        // ===== Scheduler =====
        NotificationScheduler schedule = new NotificationScheduler(app.getTimeCapsuleService(),app.getConnectionRegistry());
        schedule.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            schedule.stop();
            app.shutdown();
        }));
    }
}