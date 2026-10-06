package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.Mention.MentionRequestDTO;
import app.enums.TargetType;
import app.testUtils.TestPopulator;
import app.utils.security.JWTToken;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MentionEndpointTest {

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
    void createMention_Success() {
        MentionRequestDTO requestDTO = new MentionRequestDTO(
                seeded.memory1().getMemoryId(),
                TargetType.MEMORY,
                0,
                15,
                "wedding night",
                seeded.person1().getPersonId(),
                TargetType.PERSON
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/mentions")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("mentionId", notNullValue())
                .body("ownerId", equalTo(seeded.memory1().getMemoryId()))
                .body("ownerType", equalTo("MEMORY"))
                .body("targetId", equalTo(seeded.person1().getPersonId()))
                .body("targetType", equalTo("PERSON"))
                .body("selectedText", equalTo("wedding night"));
    }

    // ===== Read =====

    @Test
    void getOutgoingMentions_Success() {
        // mention1 owner in TestPopulator: MEMORY, memory1
        given()
                .header("Authorization", userToken)
                .when()
                .get("/mentions/owner/MEMORY/" + seeded.memory1().getMemoryId())
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].ownerId", equalTo(seeded.memory1().getMemoryId()))
                .body("[0].ownerType", equalTo("MEMORY"));
    }

    @Test
    void getIncomingMentions_Success() {
        // mention1 target in TestPopulator: PERSON, person1
        given()
                .header("Authorization", userToken)
                .when()
                .get("/mentions/target/PERSON/" + seeded.person1().getPersonId())
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].targetId", equalTo(seeded.person1().getPersonId()))
                .body("[0].targetType", equalTo("PERSON"));
    }

    // ===== Delete =====

    @Test
    void deleteMention_Success() {
        int mentionId = seeded.mention1().getMentionId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/mentions/" + mentionId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.getCode()); // 204
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createMention_Unauthorized_WithoutToken() {
        MentionRequestDTO requestDTO = new MentionRequestDTO(
                seeded.memory1().getMemoryId(),
                TargetType.MEMORY,
                0,
                5,
                "test",
                seeded.person1().getPersonId(),
                TargetType.PERSON
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/mentions")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    // ===== Read =====

    @Test
    void getOutgoingMentions_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/mentions/owner/MEMORY/" + seeded.memory1().getMemoryId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getOutgoingMentions_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/mentions/owner/INVALID_TYPE/1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getIncomingMentions_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/mentions/target/PERSON/" + seeded.person1().getPersonId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getIncomingMentions_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/mentions/target/INVALID_TYPE/1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteMention_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/mentions/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteMention_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/mentions/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deleteMention_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/mentions/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}