package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.devlog.DevlogRequestDTO;
import app.dtos.devlog.DevlogResponseDTO;
import app.testUtils.TestPopulator;
import app.utils.security.JWTToken;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DevlogEndpointTest {

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
    void createDevlog_Success() {
        DevlogRequestDTO devlogRequestDTO = new DevlogRequestDTO(
                "New Devlog Title",
                "New Devlog Subtitle",
                "Detailed log entry covering recent updates.",
                LocalDate.of(2019, 1, 1)

                );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(devlogRequestDTO)
                .when()
                .post("/devlogs")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("title", equalTo("New Devlog Title"))
                .log().all();
    }

    // ===== Read =====

    @Test
    void getDevlogById_Success() {
        int devlogId = seeded.devlog1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(devlogId))
                .body("title", equalTo(seeded.devlog1().getTitle()));
    }

    @Test
    void getAllDevlog_Success() {
        List<DevlogResponseDTO> devlogs = given()
                .header("Authorization", userToken)
                .when()
                .get("/devlogs")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(2))
                .body("[0].title", equalTo(seeded.devlog1().getTitle()))
                .extract()
                .jsonPath()
                .getList("", DevlogResponseDTO.class);
    }

    // ===== Update =====

    @Test
    void updateDevlog_Success() {
        int devlogId = seeded.devlog1().getId();

        DevlogRequestDTO devlogRequestDTO = new DevlogRequestDTO(
                "Updated Devlog Title",
                "Updated Subtitle",
                "Updated log content",
                LocalDate.of(2019, 1, 1)

        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(devlogRequestDTO)
                .when()
                .put("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteDevlog_Success() {
        int devlogId = seeded.devlog1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().all();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createDevlog_Unauthorized_WithoutToken() {
        DevlogRequestDTO devlogRequestDTO = new DevlogRequestDTO(
                "Test Title",
                "Test Subtitle",
                "Test Content",
                LocalDate.of(2019, 1, 1)

        );

        given()
                .contentType(ContentType.JSON)
                .body(devlogRequestDTO)
                .when()
                .post("/devlogs")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createDevlog_wrongData() {
        DevlogRequestDTO devlogRequestDTO = new DevlogRequestDTO(
                "",
                "",
                "",
                LocalDate.of(2019, 1, 1)

        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(devlogRequestDTO)
                .when()
                .post("/devlogs")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    // ===== Read =====

    @Test
    void getDevlogById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/devlogs/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getDevlogById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/devlogs/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void getDevlogById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/devlogs/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    @Test
    void getDevlogById_BelongsToAnotherUser() {
        int devlogId = seeded.devlog1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    // ===== Update =====

    @Test
    void updateDevlog_Unauthorized_WithoutToken() {
        int devlogId = seeded.devlog1().getId();

        DevlogRequestDTO devlogRequestDTO = new DevlogRequestDTO(
                "Test Title",
                "Test Subtitle",
                "Test Content",
                LocalDate.of(2019, 1, 1)

        );

        given()
                .contentType(ContentType.JSON)
                .body(devlogRequestDTO)
                .when()
                .put("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode())
                .log().all();
    }

    @Test
    void updateDevlog_WrongData() {
        DevlogRequestDTO devlogRequestDTO = new DevlogRequestDTO(
                "Test Title",
                "Test Subtitle",
                "Test Content",
                LocalDate.of(2019, 1, 1)

        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(devlogRequestDTO)
                .when()
                .put("/devlogs/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void updateDevlog_NotFound() {
        DevlogRequestDTO devlogRequestDTO = new DevlogRequestDTO(
                "Test Title",
                "Test Subtitle",
                "Test Content",
                LocalDate.of(2019, 1, 1)

        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(devlogRequestDTO)
                .when()
                .put("/devlogs/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    // ===== Delete =====

    @Test
    void deleteDevlog_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/devlogs/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteDevlog_wrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/devlogs/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void deleteDevlog_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/devlogs/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }
}