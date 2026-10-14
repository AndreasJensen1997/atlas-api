package app.utils.scheduling;

import app.services.TimeCapsuleService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotificationScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkUnlockedCapsules, 0, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void checkUnlockedCapsules() {
        try {

        } catch (Exception e) {
            log.error("Capsule check failed", e);
        }
    }
}