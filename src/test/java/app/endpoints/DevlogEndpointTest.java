package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.devlog.DevlogRequestDTO;
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
    private String adminToken;

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

        userToken = "Bearer " + JWTToken.generateToken(seeded.user1().getId());
        adminToken = "Bearer " + JWTToken.generateToken(seeded.admin().getId());
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

    // ===== Helpers =====

    private DevlogRequestDTO validDto(String title) {
        return new DevlogRequestDTO(title, "Test Subtitle", "Test Content", LocalDate.of(2019, 1, 1));
    }

    // ==========================================
    // ENDPOINT TESTS SUCCESS
    // ==========================================

    // ===== Create =====

    @Test
    void createDevlog_Admin_Success() {
        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(validDto("New Devlog Title"))
                .when()
                .post("/devlogs")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("title", equalTo("New Devlog Title"));
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
    void getDevlogById_AnotherUserCanRead() {
        // devlog1 was written by user1, but devlogs are shared with everyone
        int devlogId = seeded.devlog1().getId();
        String otherUserToken = "Bearer " + JWTToken.generateToken(seeded.user2().getId());

        given()
                .header("Authorization", otherUserToken)
                .when()
                .get("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(devlogId));
    }

    @Test
    void getAllDevlogs_ReturnsDevlogsFromAllAuthors() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/devlogs")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", is(3))
                .body("title", hasItems(
                        seeded.devlog1().getTitle(),
                        seeded.devlog2().getTitle(),
                        seeded.devlog3().getTitle()));
    }

    // ===== Update =====

    @Test
    void updateDevlog_Admin_Success() {
        int devlogId = seeded.devlog1().getId();

        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(validDto("Updated Devlog Title"))
                .when()
                .put("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("title", equalTo("Updated Devlog Title"));

        given()
                .header("Authorization", userToken)
                .when()
                .get("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("title", equalTo("Updated Devlog Title"));
    }

    // ===== Delete =====

    @Test
    void deleteDevlog_Admin_Success() {
        int devlogId = seeded.devlog1().getId();

        given()
                .header("Authorization", adminToken)
                .when()
                .delete("/devlogs/" + devlogId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

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
        given()
                .contentType(ContentType.JSON)
                .body(validDto("Test Title"))
                .when()
                .post("/devlogs")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createDevlog_NormalUser_Forbidden() {
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(validDto("Test Title"))
                .when()
                .post("/devlogs")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.getCode());
    }

    @Test
    void createDevlog_WrongData() {
        DevlogRequestDTO invalid = new DevlogRequestDTO("", "", "", LocalDate.of(2019, 1, 1));

        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(invalid)
                .when()
                .post("/devlogs")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
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
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getDevlogById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/devlogs/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updateDevlog_Unauthorized_WithoutToken() {
        given()
                .contentType(ContentType.JSON)
                .body(validDto("Test Title"))
                .when()
                .put("/devlogs/" + seeded.devlog1().getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void updateDevlog_NormalUser_Forbidden() {
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(validDto("Hacked title"))
                .when()
                .put("/devlogs/" + seeded.devlog1().getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.getCode());
    }

    @Test
    void updateDevlog_WrongData() {
        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(validDto("Test Title"))
                .when()
                .put("/devlogs/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void updateDevlog_NotFound() {
        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(validDto("Test Title"))
                .when()
                .put("/devlogs/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
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
    void deleteDevlog_NormalUser_Forbidden() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/devlogs/" + seeded.devlog1().getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.getCode());
    }

    @Test
    void deleteDevlog_WrongData() {
        given()
                .header("Authorization", adminToken)
                .when()
                .delete("/devlogs/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deleteDevlog_NotFound() {
        given()
                .header("Authorization", adminToken)
                .when()
                .delete("/devlogs/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}