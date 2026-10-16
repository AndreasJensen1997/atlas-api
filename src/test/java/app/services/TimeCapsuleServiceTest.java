package app.services;

import app.config.HibernateTestConfig;
import app.daos.user.UserDAO;
import app.daos.userOwned.NotificationDAO;
import app.daos.userOwned.TimeCapsuleDAO;
import app.entities.Notification;
import app.entities.TimeCapsule;
import app.entities.User;
import app.enums.NotificationType;
import app.mappers.NotificationMapper;
import app.mappers.TimeCapsuleMapper;
import app.testUtils.TestPopulator;
import app.utils.websocket.ConnectionRegistry;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimeCapsuleServiceTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private TimeCapsuleDAO timeCapsuleDAO;
    private NotificationDAO notificationDAO;
    private TimeCapsuleService service;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);

        timeCapsuleDAO = new TimeCapsuleDAO(emf);
        notificationDAO = new NotificationDAO(emf);
        UserService userService = new UserService(new UserDAO(emf));
        NotificationService notificationService = new NotificationService(notificationDAO, userService, new NotificationMapper(), new ConnectionRegistry());

        service = new TimeCapsuleService(timeCapsuleDAO, notificationService, userService, new TimeCapsuleMapper());    }

    // ===== Helpers =====

    private TimeCapsule createCapsule(User owner, LocalDate unlockDate, boolean notified) {
        return timeCapsuleDAO.create(TimeCapsule.builder()
                .title("test capsule")
                .subtitle("test subtitle")
                .content("secret")
                .unlockDate(unlockDate)
                .unlockNotified(notified)
                .user(owner)
                .build());
    }

    // Only the TIME_CAPSULE notifications that point at this exact capsule.
    // The populator also seeds other notifications, so we must filter.
    private List<Notification> notificationsFor(User owner, TimeCapsule capsule) {
        return notificationDAO.getAllByUserId(owner.getId()).stream()
                .filter(n -> n.getNotificationType() == NotificationType.TIME_CAPSULE)
                .filter(n -> capsule.getId().equals(n.getTargetId()))
                .toList();
    }

    // ===== notifyUnlockedCapsules =====

    @Test
    void notifyUnlockedCapsules_UnlockedCapsule_CreatesNotificationAndMarksCapsule() {
        User owner = seeded.user1();
        TimeCapsule capsule = createCapsule(owner, LocalDate.now().minusDays(1), false);

        service.notifyUnlockedCapsules();

        List<Notification> notifications = notificationsFor(owner, capsule);
        assertThat(notifications, hasSize(1));
        assertThat(notifications.get(0).getTargetType(), is("TIME_CAPSULE"));
        assertThat(notifications.get(0).getTitle(), containsString("test capsule"));

        TimeCapsule reloaded = timeCapsuleDAO.getById(capsule.getId());
        assertThat(reloaded.isUnlockNotified(), is(true));
    }

    @Test
    void notifyUnlockedCapsules_CalledTwice_DoesNotCreateDuplicate() {
        User owner = seeded.user1();
        TimeCapsule capsule = createCapsule(owner, LocalDate.now().minusDays(1), false);

        service.notifyUnlockedCapsules();
        service.notifyUnlockedCapsules();

        assertThat(notificationsFor(owner, capsule), hasSize(1));
    }

    @Test
    void notifyUnlockedCapsules_StillLockedCapsule_IsIgnored() {
        User owner = seeded.user1();
        TimeCapsule capsule = createCapsule(owner, LocalDate.now().plusDays(5), false);

        service.notifyUnlockedCapsules();

        assertThat(notificationsFor(owner, capsule), is(empty()));
        assertThat(timeCapsuleDAO.getById(capsule.getId()).isUnlockNotified(), is(false));
    }

    @Test
    void notifyUnlockedCapsules_AlreadyNotifiedCapsule_IsIgnored() {
        User owner = seeded.user1();
        TimeCapsule capsule = createCapsule(owner, LocalDate.now().minusDays(1), true);

        service.notifyUnlockedCapsules();

        assertThat(notificationsFor(owner, capsule), is(empty()));
    }
}