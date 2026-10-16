package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.daos.userOwned.NotificationDAO;
import app.daos.userOwned.TimeCapsuleDAO;
import app.dtos.devlog.DevlogRequestDTO;
import app.entities.TimeCapsule;
import app.enums.NotificationType;
import app.testUtils.TestPopulator;
import app.utils.security.JWTToken;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationWebSocketTest {

    private static Javalin app;
    private static EntityManagerFactory emf;
    private static ApplicationConfig applicationConfig;

    private TestPopulator.SeededData seeded;

    // Completed by the WebSocket listener, so the test can wait for them
    private CompletableFuture<String> receivedMessage;
    private CompletableFuture<Integer> closeCode;

    @BeforeAll
    static void init() {
        emf = HibernateTestConfig.getEntityManagerFactory();
        applicationConfig = new ApplicationConfig(emf);
        app = applicationConfig.startServer(7777);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7777;
        RestAssured.basePath = "/api";
    }

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);
        receivedMessage = new CompletableFuture<>();
        closeCode = new CompletableFuture<>();
    }

    @AfterEach
    void tearDown() {
        TestPopulator.cleanup(emf);
    }

    @AfterAll
    static void shutDown() {
        if (app != null) {
            applicationConfig.stopServer(app);
        }
    }

    // ===== Helper =====

    private WebSocket connect(String token) {
        WebSocket webSocket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:7777/ws/notifications?token=" + token),
                        new WebSocket.Listener() {
                            @Override
                            public void onOpen(WebSocket ws) {
                                ws.request(1);
                            }

                            @Override
                            public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                                receivedMessage.complete(data.toString());
                                ws.request(1);
                                return null;
                            }

                            @Override
                            public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                                closeCode.complete(statusCode);
                                return null;
                            }
                        })
                .join();

        // Give the server a moment to register the connection before the test triggers a push
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return webSocket;
    }

    private void waitForDevlogNotifications(int expectedCount) throws InterruptedException {
        NotificationDAO notificationDAO = new NotificationDAO(emf);
        long deadline = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < deadline) {
            long count = notificationDAO.getAll().stream()
                    .filter(n -> n.getNotificationType() == NotificationType.DEVLOG)
                    .filter(n -> n.getTitle().contains("Live update"))
                    .count();
            if (count >= expectedCount) {
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("Fan-out did not finish within 5 seconds");
    }

    // ===== Tests =====

    @Test
    void newDevlog_IsPushedToConnectedUser() throws Exception {
        String userToken = JWTToken.generateToken(seeded.user1().getId());
        String adminToken = "Bearer " + JWTToken.generateToken(seeded.admin().getId());

        connect(userToken);

        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(new DevlogRequestDTO("Live update", "Sub", "Content", LocalDate.now()))
                .when()
                .post("/devlogs")
                .then()
                .statusCode(201);

        // The fan-out runs on a background thread, so wait for it (max 5 seconds)
        String message = receivedMessage.get(5, TimeUnit.SECONDS);

        assertThat(message, containsString("DEVLOG"));
        assertThat(message, containsString("Live update"));

        waitForDevlogNotifications(4);
    }

    @Test
    void unlockedTimeCapsule_IsPushedToItsOwner() throws Exception {
        TimeCapsuleDAO timeCapsuleDAO = new TimeCapsuleDAO(emf);
        timeCapsuleDAO.create(TimeCapsule.builder()
                .title("Old capsule")
                .subtitle("Sub")
                .content("Secret")
                .unlockDate(LocalDate.now().minusDays(1))
                .user(seeded.user1())
                .build());

        connect(JWTToken.generateToken(seeded.user1().getId()));

        applicationConfig.getTimeCapsuleService().notifyUnlockedCapsules();

        String message = receivedMessage.get(5, TimeUnit.SECONDS);

        assertThat(message, containsString("TIME_CAPSULE"));
        assertThat(message, containsString("Old capsule"));
    }

    @Test
    void invalidToken_ConnectionIsClosed() throws Exception {
        connect("not-a-valid-token");

        Integer code = closeCode.get(5, TimeUnit.SECONDS);

        assertThat(code, is(4401));
    }

    @Test
    void missingToken_ConnectionIsClosed() throws Exception {
        connect("");

        Integer code = closeCode.get(5, TimeUnit.SECONDS);

        assertThat(code, is(4401));
    }
}