package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.timeCapsule.TimeCapsuleRequestDTO;
import app.dtos.timeCapsule.TimeCapsuleResponseDTO;
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
public class TimeCapsuleEndpointTest {

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

        // 2. Start Javalin on dedicated test port (7777)
        app = applicationConfig.startServer(7777);

        // 3. RestAssured configuration
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
    void createTimeCapsule_Success() {
        TimeCapsuleRequestDTO timeCapsuleRequestDTO = new TimeCapsuleRequestDTO(
                "Message to the Future",
                "Open in 2030",
                "Secret contents for future self",
                LocalDate.now().plusYears(5)
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(timeCapsuleRequestDTO)
                .when()
                .post("/time-capsules")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("title", equalTo("Message to the Future"));
    }

    // ===== Read =====

    @Test
    void getTimeCapsuleById_Success() {
        int timeCapsuleId = seeded.timeCapsule1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/time-capsules/" + timeCapsuleId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(timeCapsuleId))
                .body("title", equalTo(seeded.timeCapsule1().getTitle()));
    }

    @Test
    void getAllTimeCapsules_Success() {
        List<TimeCapsuleResponseDTO> capsules = given()
                .header("Authorization", userToken)
                .when()
                .get("/time-capsules")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(1))
                .body("[0].title", equalTo(seeded.timeCapsule1().getTitle()))
                .extract()
                .jsonPath()
                .getList("", TimeCapsuleResponseDTO.class);
    }

    // ===== Delete =====

    @Test
    void deleteTimeCapsule_Success() {
        int timeCapsuleId = seeded.timeCapsule1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/time-capsules/" + timeCapsuleId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

        // Verify resource deletion
        given()
                .header("Authorization", userToken)
                .when()
                .get("/time-capsules/" + timeCapsuleId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createTimeCapsule_Unauthorized_WithoutToken() {
        TimeCapsuleRequestDTO timeCapsuleRequestDTO = new TimeCapsuleRequestDTO(
                "Test Title",
                "Test Subtitle",
                "Test Content",
                LocalDate.now().plusYears(1)
        );

        given()
                .contentType(ContentType.JSON)
                .body(timeCapsuleRequestDTO)
                .when()
                .post("/time-capsules")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createTimeCapsule_WrongData() {
        TimeCapsuleRequestDTO timeCapsuleRequestDTO = new TimeCapsuleRequestDTO(
                "",
                "Test Subtitle",
                "Test Content",
                null
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(timeCapsuleRequestDTO)
                .when()
                .post("/time-capsules")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Read =====

    @Test
    void getTimeCapsuleById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/time-capsules/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getTimeCapsuleById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/time-capsules/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getTimeCapsuleById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/time-capsules/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void getTimeCapsuleById_BelongsToAnotherUser() {
        int timeCapsuleId = seeded.timeCapsule1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/time-capsules/" + timeCapsuleId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updateTimeCapsule_Unauthorized_WithoutToken() {
        int timeCapsuleId = seeded.timeCapsule1().getId();

        TimeCapsuleRequestDTO timeCapsuleRequestDTO = new TimeCapsuleRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                "Updated Content",
                LocalDate.now().plusYears(2)
        );

        given()
                .contentType(ContentType.JSON)
                .body(timeCapsuleRequestDTO)
                .when()
                .put("/time-capsules/" + timeCapsuleId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }


    @Test
    void updateTimeCapsule_NotFound() {
        TimeCapsuleRequestDTO timeCapsuleRequestDTO = new TimeCapsuleRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                "Updated Content",
                LocalDate.now().plusYears(2)
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(timeCapsuleRequestDTO)
                .when()
                .put("/time-capsules/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteTimeCapsule_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/time-capsules/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteTimeCapsule_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/time-capsules/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deleteTimeCapsule_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/time-capsules/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}