package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.fragment.FragmentRequestDTO;
import app.dtos.fragment.FragmentResponseDTO;
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
public class FragmentEndpointTest {

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
    void createFragment_Success() {
        FragmentRequestDTO fragmentRequestDTO = new FragmentRequestDTO(
                "New Fragment Title",
                "New Subtitle",
                "New Content"
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(fragmentRequestDTO)
                .when()
                .post("/fragments")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("title", equalTo("New Fragment Title"));
    }

    // ===== Read =====

    @Test
    void getFragmentById_Success() {
        int fragmentId = seeded.fragment1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/fragments/" + fragmentId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(fragmentId))
                .body("title", equalTo(seeded.fragment1().getTitle()));
    }

    @Test
    void getAllFragments_Success() {
        List<FragmentResponseDTO> fragments = given()
                .header("Authorization", userToken)
                .when()
                .get("/fragments")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(2))
                .body("[0].title", equalTo(seeded.fragment1().getTitle()))
                .extract()
                .jsonPath()
                .getList("", FragmentResponseDTO.class);
    }

    // ===== Update =====

    @Test
    void updateFragment_Success() {
        int fragmentId = seeded.fragment1().getId();

        FragmentRequestDTO fragmentRequestDTO = new FragmentRequestDTO(
                "Updated Fragment Title",
                "Updated Subtitle",
                "Updated Content"
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(fragmentRequestDTO)
                .when()
                .put("/fragments/" + fragmentId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteFragment_Success() {
        int fragmentId = seeded.fragment1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/fragments/" + fragmentId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

        // Verify resource deletion
        given()
                .header("Authorization", userToken)
                .when()
                .get("/fragments/" + fragmentId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createFragment_Unauthorized_WithoutToken() {
        FragmentRequestDTO fragmentRequestDTO = new FragmentRequestDTO(
                "Test Title",
                "Test Subtitle",
                "Test Content"
        );

        given()
                .contentType(ContentType.JSON)
                .body(fragmentRequestDTO)
                .when()
                .post("/fragments")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createFragment_WrongData() {
        FragmentRequestDTO fragmentRequestDTO = new FragmentRequestDTO(
                "test title",
                "test subtitle",
                ""
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(fragmentRequestDTO)
                .when()
                .post("/fragments")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Read =====

    @Test
    void getFragmentById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/fragments/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getFragmentById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/fragments/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getFragmentById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/fragments/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void getFragmentById_BelongsToAnotherUser() {
        int fragmentId = seeded.fragment1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/fragments/" + fragmentId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updateFragment_Unauthorized_WithoutToken() {
        int fragmentId = seeded.fragment1().getId();

        FragmentRequestDTO fragmentRequestDTO = new FragmentRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                "Updated Content"
        );

        given()
                .contentType(ContentType.JSON)
                .body(fragmentRequestDTO)
                .when()
                .put("/fragments/" + fragmentId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void updateFragment_WrongData() {
        FragmentRequestDTO fragmentRequestDTO = new FragmentRequestDTO(
                "",
                "",
                "Updated Content"
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(fragmentRequestDTO)
                .when()
                .put("/fragments/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void updateFragment_NotFound() {
        FragmentRequestDTO fragmentRequestDTO = new FragmentRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                "Updated Content"
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(fragmentRequestDTO)
                .when()
                .put("/fragments/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteFragment_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/fragments/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteFragment_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/fragments/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deleteFragment_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/fragments/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}