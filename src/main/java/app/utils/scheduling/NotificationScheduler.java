package app.utils.scheduling;

import app.entities.TimeCapsule;
import app.services.TimeCapsuleService;
import app.utils.websocket.ConnectionRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotificationScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConnectionRegistry connectionRegistry;

    private final TimeCapsuleService timeCapsuleService;

    public NotificationScheduler(TimeCapsuleService timeCapsuleService, ConnectionRegistry connectionRegistry) {
        this.timeCapsuleService = timeCapsuleService;
        this.connectionRegistry = connectionRegistry;
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(this::checkUnlockedCapsules, 0, 1, TimeUnit.MINUTES);
        scheduler.scheduleWithFixedDelay(this::pingConnections, 0, 20, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void checkUnlockedCapsules() {
        try {
            List<TimeCapsule> processed = timeCapsuleService.notifyUnlockedCapsules();
            if (!processed.isEmpty()) {
                log.info("Notified {} unlocked time capsule(s)", processed.size());
            }
        } catch (Exception e) {
            log.error("Capsule check failed", e);
        }
    }

    private void pingConnections() {
        try {
            connectionRegistry.pingAll();
        } catch (Exception e) {
            log.error("Ping failed", e);
        }
    }
}