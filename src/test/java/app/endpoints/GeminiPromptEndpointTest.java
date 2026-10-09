package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.geminiPrompt.GeminiPromptRequestDTO;
import app.dtos.geminiPrompt.GeminiPromptResponseDTO;
import app.dtos.geminiPrompt.GeminiPromptSaveDTO;
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
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GeminiPromptEndpointTest {

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

        // 2. Start Javalin server on dedicated test port (7777)
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

    // ===== Generate Prompt =====

//    @Test
//    void generatePrompt_Success() {
//        GeminiPromptRequestDTO requestDTO = new GeminiPromptRequestDTO("Give me 3 book recommendations");
//
//        given()
//                .header("Authorization", userToken)
//                .contentType(ContentType.JSON)
//                .body(requestDTO)
//                .when()
//                .post("/gemini-prompts/generate")
//                .then()
//                .statusCode(HttpStatus.OK.getCode())
//                .body("promptText", notNullValue())
//                .log().all();
//    }

    // ===== Save Prompt =====

    @Test
    void savePrompt_Success() {
        GeminiPromptSaveDTO saveDTO = new GeminiPromptSaveDTO(
                "1. Paris, France\n2. Rome, Italy\n3. Barcelona, Spain"
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(saveDTO)
                .when()
                .post("/gemini-prompts")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("content", equalTo("1. Paris, France\n2. Rome, Italy\n3. Barcelona, Spain"))
                .log().all();
    }

    // ===== Read =====

    @Test
    void getPromptById_Success() {
        int promptId = seeded.geminiPrompt1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/gemini-prompts/" + promptId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(promptId))
                .body("content", equalTo(seeded.geminiPrompt1().getContent()));
    }

    @Test
    void getAllPrompts_Success() {
        // user1 owns geminiPrompt1 and geminiPrompt2 -> size should be 2
        List<GeminiPromptResponseDTO> prompts = given()
                .header("Authorization", userToken)
                .when()
                .get("/gemini-prompts")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(2))
                .body("content", hasItems(
                        seeded.geminiPrompt1().getContent(),
                        seeded.geminiPrompt2().getContent()
                ))
                .extract()
                .jsonPath()
                .getList("", GeminiPromptResponseDTO.class);
    }

    // ===== Delete =====

    @Test
    void deletePrompt_Success() {
        int promptId = seeded.geminiPrompt1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/gemini-prompts/" + promptId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().all();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/gemini-prompts/" + promptId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Generate Prompt =====

    @Test
    void generatePrompt_Unauthorized_WithoutToken() {
        Map<String, String> requestBody = Map.of("prompt", "Hello Gemini");

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/gemini-prompts/generate")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void generatePrompt_WrongData_BlankPrompt() {
        Map<String, String> requestBody = Map.of("prompt", "");

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/gemini-prompts/generate")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    // ===== Save Prompt =====

    @Test
    void savePrompt_Unauthorized_WithoutToken() {
        GeminiPromptSaveDTO saveDTO = new GeminiPromptSaveDTO("Saved response content");

        given()
                .contentType(ContentType.JSON)
                .body(saveDTO)
                .when()
                .post("/gemini-prompts")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void savePrompt_WrongData_BlankContent() {
        GeminiPromptSaveDTO saveDTO = new GeminiPromptSaveDTO("");

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(saveDTO)
                .when()
                .post("/gemini-prompts")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    // ===== Read =====

    @Test
    void getPromptById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/gemini-prompts/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getPromptById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/gemini-prompts/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void getPromptById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/gemini-prompts/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    @Test
    void getPromptById_BelongsToAnotherUser() {
        // geminiPrompt3 belongs to user2, attempting access with user1 token returns 404
        int prompt3Id = seeded.geminiPrompt3().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/gemini-prompts/" + prompt3Id)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }

    // ===== Delete =====

    @Test
    void deletePrompt_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/gemini-prompts/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deletePrompt_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/gemini-prompts/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode())
                .log().all();
    }

    @Test
    void deletePrompt_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/gemini-prompts/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode())
                .log().all();
    }
}