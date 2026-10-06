package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.artifact.ArtifactRequestDTO;
import app.dtos.artifact.ArtifactResponseDTO;
import app.enums.Visibility;
import app.testUtils.TestPopulator;
import app.utils.security.JWTToken;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArtifactEndpointTest {

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
    void createArtifact_Success() {
        ArtifactRequestDTO artifactRequestDTO = new ArtifactRequestDTO(
                "New Artifact Title",
                "New Subtitle",
                "New Content",
                Visibility.PUBLIC,
                seeded.artifact1().getArtifactType()
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(artifactRequestDTO)
                .when()
                .post("/artifacts")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("artifactId", notNullValue())
                .body("title", equalTo("New Artifact Title"));
    }

    // ===== Read =====

    @Test
    void getArtifactById_Success() {
        int artifactId = seeded.artifact1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifacts/" + artifactId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("artifactId", equalTo(artifactId))
                .body("title", equalTo(seeded.artifact1().getTitle()));
    }

    @Test
    void getAllArtifacts_Success() {
        List<ArtifactResponseDTO> artifacts = given()
                .header("Authorization", userToken)
                .when()
                .get("/artifacts")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(1))
                .body("[0].title", equalTo(seeded.artifact1().getTitle()))
                .extract()
                .jsonPath()
                .getList("", ArtifactResponseDTO.class);
    }

    @Test
    void getRandomArtifact_Success() {
        List<String> expectedTitles = List.of(
                seeded.artifact1().getTitle(),
                seeded.chapter2().getTitle()
        );

        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifacts/random")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("title", in(expectedTitles));
    }

    // ===== Update =====

    @Test
    void updateArtifact_Success() {
        int artifactId = seeded.artifact1().getId();

        ArtifactRequestDTO artifactRequestDTO = new ArtifactRequestDTO(
                "Updated Artifact Title",
                "Updated Subtitle",
                "Updated Content",
                Visibility.PUBLIC,
                null
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(artifactRequestDTO)
                .when()
                .put("/artifacts/" + artifactId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteArtifact_Success() {
        int artifactId = seeded.artifact1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/artifacts/" + artifactId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

        // Verify resource deletion
        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifacts/" + artifactId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createArtifact_Unauthorized_WithoutToken() {
        ArtifactRequestDTO artifactRequestDTO = new ArtifactRequestDTO(
                "Test Title",
                "Test Subtitle",
                "Test Content",
                Visibility.PUBLIC,
                null
        );

        given()
                .contentType(ContentType.JSON)
                .body(artifactRequestDTO)
                .when()
                .post("/artifacts")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createArtifact_WrongData() {
        ArtifactRequestDTO artifactRequestDTO = new ArtifactRequestDTO(
                "",
                "",
                "Test Content",
                Visibility.PUBLIC,
                null
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(artifactRequestDTO)
                .when()
                .post("/artifacts")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Read =====

    @Test
    void getArtifactById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/artifacts/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getArtifactById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifacts/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getArtifactById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifacts/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void getArtifactById_BelongsToAnotherUser() {
        int artifactId = seeded.artifact1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/artifacts/" + artifactId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updateArtifact_Unauthorized_WithoutToken() {
        int artifactId = seeded.artifact1().getId();

        ArtifactRequestDTO artifactRequestDTO = new ArtifactRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                "Updated Content",
                Visibility.PUBLIC,
                null
        );

        given()
                .contentType(ContentType.JSON)
                .body(artifactRequestDTO)
                .when()
                .put("/artifacts/" + artifactId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void updateArtifact_WrongData() {
        ArtifactRequestDTO artifactRequestDTO = new ArtifactRequestDTO(
                "",
                "",
                "Updated Content",
                Visibility.PUBLIC,
                null
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(artifactRequestDTO)
                .when()
                .put("/artifacts/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void updateArtifact_NotFound() {
        ArtifactRequestDTO artifactRequestDTO = new ArtifactRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                "Updated Content",
                Visibility.PUBLIC,
                null
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(artifactRequestDTO)
                .when()
                .put("/artifacts/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteArtifact_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/artifacts/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteArtifact_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/artifacts/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deleteArtifact_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/artifacts/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}