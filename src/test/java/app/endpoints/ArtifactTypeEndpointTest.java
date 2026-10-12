package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.artifact.ArtifactRequestDTO;
import app.entities.ArtifactType;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArtifactTypeEndpointTest {

    private static Javalin app;
    private static EntityManagerFactory emf;
    private static ApplicationConfig applicationConfig;

    private TestPopulator.SeededData seeded;
    private String userToken;
    private String user2Token;

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

        userToken = "Bearer " + JWTToken.generateToken(seeded.user1().getEmail());
        user2Token = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());
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
    // HELPERS
    // ==========================================

    // Creating an artifact is how a user creates a new artifact type,
    // so the tests do it the same way the real application does.
    private void createArtifactWithType(String token, String title, String typeName) {
        ArtifactRequestDTO dto = new ArtifactRequestDTO(
                title,
                "Subtitle",
                "Content",
                Visibility.PRIVATE,
                ArtifactType.builder().name(typeName).build()
        );

        given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post("/artifacts")
                .then()
                .statusCode(HttpStatus.CREATED.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS SUCCESS
    // ==========================================

    // ===== Read =====

    @Test
    void getAllArtifactTypes_Success() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifact-types")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", is(1))
                .body("[0].id", equalTo(seeded.musicType().getId()))
                .body("[0].name", equalTo("Music"));
    }

    @Test
    void getAllArtifactTypes_OnlyReturnsOwnTypes() {
        // user2 owns "Physical Object" only, never user1's "Music"
        given()
                .header("Authorization", user2Token)
                .when()
                .get("/artifact-types")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", is(1))
                .body("[0].name", equalTo("Physical Object"));
    }

    @Test
    void getAllArtifactTypes_NewTypeFromArtifact_IsListedAndSorted() {
        createArtifactWithType(userToken, "My favourite shirt", "tøj");
        createArtifactWithType(userToken, "Moms necklace", "apple");

        // sorted alphabetically, case insensitive
        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifact-types")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("name", contains("apple", "Music", "tøj"));
    }

    @Test
    void getAllArtifactTypes_SameNameDifferentCase_IsNotDuplicated() {
        createArtifactWithType(userToken, "Another album", "music");   // user1 already has "Music"

        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifact-types")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", is(1))
                .body("[0].name", equalTo("Music"));
    }

    @Test
    void getAllArtifactTypes_SameNameAsOtherUser_BothUsersKeepTheirOwn() {
        createArtifactWithType(user2Token, "Vinyl record", "Music");   // user1 already has "Music"

        given()
                .header("Authorization", user2Token)
                .when()
                .get("/artifact-types")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("name", containsInAnyOrder("Physical Object", "Music"));

        given()
                .header("Authorization", userToken)
                .when()
                .get("/artifact-types")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", is(1));
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Read =====

    @Test
    void getAllArtifactTypes_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/artifact-types")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getAllArtifactTypes_Unauthorized_InvalidToken() {
        given()
                .header("Authorization", "Bearer not.a.real.token")
                .when()
                .get("/artifact-types")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }
}