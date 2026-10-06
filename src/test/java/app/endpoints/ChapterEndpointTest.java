package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.chapter.ChapterRequestDTO;
import app.dtos.chapter.ChapterResponseDTO;
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

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChapterEndpointTest {

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

        // 2. Start the Javalin server on a dedicated test port (7777)
        app = applicationConfig.startServer(7777);

        // 3. Configure RestAssured base settings
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7777;
        RestAssured.basePath = "/api";
    }

    @BeforeEach
    void setUp() {
        // Clean up and populate fresh data before every test run
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);

        // Generate a valid token for the seeded user to use in RestAssured headers
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
    void createChapter_Success() {
        ChapterRequestDTO chapterRequestDTO = new ChapterRequestDTO(
                "New Chapter",
                "Test subtitle",
                "Test content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(chapterRequestDTO)
                .when()
                .post("/chapters")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("title", equalTo("New Chapter"))
                .log().all();
    }

    // ===== Read =====

    @Test
    void getChapterById_Success() {
        int chapterId = seeded.chapter1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/chapters/" + chapterId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(chapterId))
                .body("title", equalTo(seeded.chapter1().getTitle()));
    }

    @Test
    void getAllChapter_Success() {
        List<ChapterResponseDTO> chapters = given()
                .header("Authorization", userToken)
                .when()
                .get("/chapters")
                .then()
                .statusCode(200)
                .log().body() // <--- Prints full JSON array to console
                .body("size()", is(1))
                .body("[0].title", equalTo(seeded.chapter1().getTitle()))
                .extract()
                .jsonPath()
                .getList("", ChapterResponseDTO.class);
    }

    @Test
    void getRandomChapter_Success() {
        List<String> expectedTitles = List.of(
                seeded.chapter1().getTitle(),
                seeded.chapter2().getTitle()
        );

        given()
                .header("Authorization", userToken)
                .when()
                .get("/chapters/random")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("title", in(expectedTitles));
    }

    // ===== Update =====

    @Test
    void updateChapter_Success() {
        int chapterId = seeded.chapter1().getId();

        ChapterRequestDTO chapterRequestDTO = new ChapterRequestDTO(
                "Test title",
                "Test subtitle",
                "Test content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(chapterRequestDTO)
                .when()
                .put("/chapters/" + chapterId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteChapter_Success() {
        int chapterId = seeded.chapter1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/chapters/" + chapterId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().all();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/chapters/" + chapterId)
                .then()
                .statusCode(404);
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createChapter_Unauthorized_WithoutToken() {

        ChapterRequestDTO chapterRequestDTO = new ChapterRequestDTO(
                "Test title",
                "Test subtitle",
                "Test content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );
        given()
                .contentType(ContentType.JSON)
                .body(chapterRequestDTO)
                .when()
                .post("/chapters")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createChapter_wrongData() {

        ChapterRequestDTO chapterRequestDTO = new ChapterRequestDTO(
                "",
                "",
                "Test content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(chapterRequestDTO)
                .when()
                .post("/chapters")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    // ===== Read =====

    @Test
    void getChapterById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/chapters/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getChapterById_WrongData() {
        given()
                .when()
                .header("Authorization", userToken)
                .get("/chapters/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void getChapterById_NotFound() {
        given()
                .when()
                .header("Authorization", userToken)
                .get("/chapters/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    @Test
    void getChapterById_BelongsToAnotherUser() {
        int chapterId = seeded.chapter1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/chapters/" + chapterId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();


    }

    // ===== Update =====

    @Test
    void updateChapter_Unauthorized_WithoutToken() {
        int chapterId = seeded.chapter1().getId();

        ChapterRequestDTO chapterRequestDTO = new ChapterRequestDTO(
                "Test title",
                "Test subtitle",
                "Test content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );
        given()
                .contentType(ContentType.JSON)
                .body(chapterRequestDTO)
                .when()
                .put("/chapters/" + chapterId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode())
                .log().all();
    }

    @Test
    void updateChapter_WrongData() {

        ChapterRequestDTO chapterRequestDTO = new ChapterRequestDTO(
                "Test title",
                "Test subtitle",
                "Test content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(chapterRequestDTO)
                .when()
                .put("/chapters/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void updateChapter_NotFound() {

        ChapterRequestDTO chapterRequestDTO = new ChapterRequestDTO(
                "Test title",
                "Test subtitle",
                "Test content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(chapterRequestDTO)
                .when()
                .put("/chapters/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    // ===== Delete =====

    @Test
    void deleteChapter_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/chapters/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteChapter_wrongData() {

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/chapters/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void deleteChapter_NotFound() {

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/chapters/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }
}