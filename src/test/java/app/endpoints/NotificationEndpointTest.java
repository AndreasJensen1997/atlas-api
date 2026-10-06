package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.notification.NotificationRequestDTO;
import app.dtos.notification.NotificationResponseDTO;
import app.enums.NotificationType;
import app.testUtils.TestPopulator;
import app.utils.security.JWTToken;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationEndpointTest {

    private static Javalin app;
    private static EntityManagerFactory emf;
    private static ApplicationConfig applicationConfig;

    private TestPopulator.SeededData seeded;
    private String userToken;

    @BeforeAll
    static void init() {
        // 1. Initialize test DB factory
        emf = HibernateTestConfig.getEntityManagerFactory();
        applicationConfig = new ApplicationConfig(emf);

        // 2. Start Javalin server on dedicated test port
        app = applicationConfig.startServer(7777);

        // 3. Configure RestAssured base settings
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7777;
        RestAssured.basePath = "/api";
    }

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);

        userToken = "Bearer " + JWTToken.generateToken(
                seeded.user1().getEmail()
        );
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

    // ==========================================
    // ENDPOINT TESTS SUCCESS
    // ==========================================

    // ===== Create =====

    @Test
    void createNotification_Success() {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
                "New comment on your post",
                NotificationType.MENTION,
                10,
                "COMMENT",
                false,
                LocalDateTime.now()
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/notifications")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("notificationId", notNullValue())
                .body("title", equalTo("New comment on your post"))
                .body("notificationType", equalTo("MENTION"))
                .log().all();
    }

    // ===== Read =====

    @Test
    void getNotificationById_Success() {
        int notificationId = seeded.notification1().getNotificationId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/notifications/" + notificationId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("notificationId", equalTo(notificationId))
                .body("title", equalTo("new friend request"))
                .body("notificationType", equalTo("FRIEND_REQUEST"));
    }

    @Test
    void getAllNotification_Success() {
        // user1 owns notification1 and notification2 -> size should be 2
        List<NotificationResponseDTO> notifications = given()
                .header("Authorization", userToken)
                .when()
                .get("/notifications")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(2))
                .body("title", hasItems("new friend request", "system patch"))
                .extract()
                .jsonPath()
                .getList("", NotificationResponseDTO.class);
    }

    // ===== Update =====

    @Test
    void updateNotification_Success() {
        int notificationId = seeded.notification1().getNotificationId();

        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
                "Updated Notification Title",
                NotificationType.FRIEND_REQUEST,
                1,
                "USER",
                true,
                LocalDateTime.now()
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/notifications/" + notificationId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteNotification_Success() {
        int notificationId = seeded.notification1().getNotificationId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/notifications/" + notificationId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().all();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/notifications/" + notificationId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createNotification_Unauthorized_WithoutToken() {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
                "Unauthorized Notification",
                NotificationType.SYSTEM,
                null,
                null,
                false,
                LocalDateTime.now()
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/notifications")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createNotification_wrongData() {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
                "",
                null,
                null,
                null,
                false,
                LocalDateTime.now()
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/notifications")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    // ===== Read =====

    @Test
    void getNotificationById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/notifications/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getNotificationById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/notifications/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void getNotificationById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/notifications/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    @Test
    void getNotificationById_BelongsToAnotherUser() {
        // notification3 belongs to user2, attempting access with user1 token returns 404
        int notification3Id = seeded.notification3().getNotificationId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/notifications/" + notification3Id)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    // ===== Update =====

    @Test
    void updateNotification_Unauthorized_WithoutToken() {
        int notificationId = seeded.notification1().getNotificationId();

        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
                "Updated Title",
                NotificationType.SYSTEM,
                null,
                null,
                true,
                LocalDateTime.now()
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/notifications/" + notificationId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode())
                .log().all();
    }

    @Test
    void updateNotification_WrongData() {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
                "Test Title",
                NotificationType.SYSTEM,
                null,
                null,
                false,
                LocalDateTime.now()
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/notifications/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void updateNotification_NotFound() {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO(
                "Test Title",
                NotificationType.SYSTEM,
                null,
                null,
                false,
                LocalDateTime.now()
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/notifications/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    // ===== Delete =====

    @Test
    void deleteNotification_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/notifications/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteNotification_wrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/notifications/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void deleteNotification_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/notifications/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }
}