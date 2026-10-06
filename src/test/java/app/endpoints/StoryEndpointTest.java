package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.story.StoryRequestDTO;
import app.dtos.story.StoryResponseDTO;
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
public class StoryEndpointTest {

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
    void createStory_Success() {
        StoryRequestDTO storyRequestDTO = new StoryRequestDTO(
                "New Story Title",
                "New Story subtitle",
                "New Story content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(storyRequestDTO)
                .when()
                .post("/stories")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("title", equalTo("New Story Title"));
    }

    // ===== Read =====

    @Test
    void getStoryById_Success() {
        int storyId = seeded.story1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/stories/" + storyId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(storyId))
                .body("title", equalTo(seeded.story1().getTitle()));
    }

    @Test
    void getAllStories_Success() {
        List<StoryResponseDTO> stories = given()
                .header("Authorization", userToken)
                .when()
                .get("/stories")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(1))
                .body("[0].title", equalTo(seeded.story1().getTitle()))
                .extract()
                .jsonPath()
                .getList("", StoryResponseDTO.class);
    }

    @Test
    void getRandomChapter_Success() {
        List<String> expectedTitles = List.of(
                seeded.story1().getTitle(),
                seeded.story2().getTitle()
        );

        given()
                .header("Authorization", userToken)
                .when()
                .get("/stories/random")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("title", in(expectedTitles));
    }

    // ===== Update =====

    @Test
    void updateStory_Success() {
        int storyId = seeded.story1().getId();

        StoryRequestDTO storyRequestDTO = new StoryRequestDTO(
                "Updated Story Title",
                "Updated Story Content",
                "Updated story content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(storyRequestDTO)
                .when()
                .put("/stories/" + storyId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteStory_Success() {
        int storyId = seeded.story1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/stories/" + storyId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

        // Verify resource deletion
        given()
                .header("Authorization", userToken)
                .when()
                .get("/stories/" + storyId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createStory_Unauthorized_WithoutToken() {
        StoryRequestDTO storyRequestDTO = new StoryRequestDTO(
                "Test Story Title",
                "Test Story Content",
                "Updated story content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );

        given()
                .contentType(ContentType.JSON)
                .body(storyRequestDTO)
                .when()
                .post("/stories")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createStory_WrongData() {
        StoryRequestDTO storyRequestDTO = new StoryRequestDTO(
                "",
                "",
                "",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(storyRequestDTO)
                .when()
                .post("/stories")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Read =====

    @Test
    void getStoryById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/stories/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getStoryById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/stories/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getStoryById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/stories/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void getStoryById_BelongsToAnotherUser() {
        int storyId = seeded.story1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/stories/" + storyId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updateStory_Unauthorized_WithoutToken() {
        int storyId = seeded.story1().getId();

        StoryRequestDTO storyRequestDTO = new StoryRequestDTO(
                "Updated Title",
                "Updated Content",
                "updated content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );

        given()
                .contentType(ContentType.JSON)
                .body(storyRequestDTO)
                .when()
                .put("/stories/" + storyId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void updateStory_WrongData() {
        StoryRequestDTO storyRequestDTO = new StoryRequestDTO(
                "",
                "",
                "Updated story content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(storyRequestDTO)
                .when()
                .put("/stories/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void updateStory_NotFound() {
        StoryRequestDTO storyRequestDTO = new StoryRequestDTO(
                "Updated Story Title",
                "Updated Story Content",
                "Updated story content",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(storyRequestDTO)
                .when()
                .put("/stories/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteStory_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/stories/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteStory_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/stories/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deleteStory_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/stories/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}