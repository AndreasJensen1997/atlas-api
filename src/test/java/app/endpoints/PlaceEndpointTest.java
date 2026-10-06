package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.place.PlaceRequestDTO;
import app.dtos.place.PlaceResponseDTO;
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
public class PlaceEndpointTest {

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
    void createPlace_Success() {
        PlaceRequestDTO placeRequestDTO = new PlaceRequestDTO(
                "Central Park",
                "A large public park in New York City",
                40.785091,
                -73.968285,
                "59th to 110th St",
                "New York",
                "USA",
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(placeRequestDTO)
                .when()
                .post("/places")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("name", equalTo("Central Park"));
    }

    // ===== Read =====

    @Test
    void getPlaceById_Success() {
        int placeId = seeded.place1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/places/" + placeId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(placeId))
                .body("name", equalTo(seeded.place1().getName()));
    }

    @Test
    void getAllPlaces_Success() {
        List<PlaceResponseDTO> places = given()
                .header("Authorization", userToken)
                .when()
                .get("/places")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(1))
                .body("[0].name", equalTo(seeded.place1().getName()))
                .extract()
                .jsonPath()
                .getList("", PlaceResponseDTO.class);
    }

    @Test
    void getRandomPlace_Success() {
        List<String> expectedNames = List.of(
                seeded.place1().getName(),
                seeded.place2().getName()
        );

        given()
                .header("Authorization", userToken)
                .when()
                .get("/places/random")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("name", in(expectedNames));
    }

    // ===== Update =====

    @Test
    void updatePlace_Success() {
        int placeId = seeded.place1().getId();

        PlaceRequestDTO placeRequestDTO = new PlaceRequestDTO(
                "Updated Place Name",
                "Updated Content",
                55.676098,
                12.568337,
                "Updated Address",
                "Copenhagen",
                "Denmark",
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(placeRequestDTO)
                .when()
                .put("/places/" + placeId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deletePlace_Success() {
        int placeId = seeded.place1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/places/" + placeId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

        // Verify resource deletion
        given()
                .header("Authorization", userToken)
                .when()
                .get("/places/" + placeId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createPlace_Unauthorized_WithoutToken() {
        PlaceRequestDTO placeRequestDTO = new PlaceRequestDTO(
                "Test Place",
                "Test Content",
                0.0,
                0.0,
                "Test Address",
                "Test City",
                "Test Country",
                Visibility.PUBLIC
        );

        given()
                .contentType(ContentType.JSON)
                .body(placeRequestDTO)
                .when()
                .post("/places")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createPlace_WrongData() {
        PlaceRequestDTO placeRequestDTO = new PlaceRequestDTO(
                "",
                "Test Content",
                null,
                null,
                "",
                "",
                "",
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(placeRequestDTO)
                .when()
                .post("/places")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    // ===== Read =====

    @Test
    void getPlaceById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/places/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getPlaceById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/places/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getPlaceById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/places/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void getPlaceById_BelongsToAnotherUser() {
        int placeId = seeded.place1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/places/" + placeId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updatePlace_Unauthorized_WithoutToken() {
        int placeId = seeded.place1().getId();

        PlaceRequestDTO placeRequestDTO = new PlaceRequestDTO(
                "Updated Name",
                "Updated Content",
                0.0,
                0.0,
                "Address",
                "City",
                "Country",
                Visibility.PUBLIC
        );

        given()
                .contentType(ContentType.JSON)
                .body(placeRequestDTO)
                .when()
                .put("/places/" + placeId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void updatePlace_WrongData() {
        PlaceRequestDTO placeRequestDTO = new PlaceRequestDTO(
                "",
                "Updated Content",
                null,
                null,
                "",
                "",
                "",
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(placeRequestDTO)
                .when()
                .put("/places/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void updatePlace_NotFound() {
        PlaceRequestDTO placeRequestDTO = new PlaceRequestDTO(
                "Updated Name",
                "Updated Content",
                0.0,
                0.0,
                "Address",
                "City",
                "Country",
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(placeRequestDTO)
                .when()
                .put("/places/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Delete =====

    @Test
    void deletePlace_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/places/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deletePlace_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/places/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deletePlace_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/places/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}