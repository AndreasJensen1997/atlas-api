package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.memory.MemoryRequestDTO;
import app.dtos.memory.MemoryResponseDTO;
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
public class MemoryEndpointTest {

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
    void createMemory_Success() {
        MemoryRequestDTO memoryRequestDTO = new MemoryRequestDTO(
                "New Memory Title",
                "New Memory Subtitle",
                "New Memory Content",
                LocalDate.of(2021, 5, 10),
                LocalDate.now(),
                LocalDate.now(),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(memoryRequestDTO)
                .when()
                .post("/memories")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("title", equalTo("New Memory Title"));
    }

    // ===== Read =====

    @Test
    void getMemoryById_Success() {
        int memoryId = seeded.memory1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/memories/" + memoryId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(memoryId))
                .body("title", equalTo(seeded.memory1().getTitle()));
    }

    @Test
    void getAllMemories_Success() {
        List<MemoryResponseDTO> memories = given()
                .header("Authorization", userToken)
                .when()
                .get("/memories")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(1))
                .body("[0].title", equalTo(seeded.memory1().getTitle()))
                .extract()
                .jsonPath()
                .getList("", MemoryResponseDTO.class);
    }

    @Test
    void getRandomMemory_Success() {
        List<String> expectedTitles = List.of(
                seeded.memory1().getTitle(),
                seeded.memory2().getTitle()
        );

        given()
                .header("Authorization", userToken)
                .when()
                .get("/memories/random")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("title", in(expectedTitles));
    }

    // ===== Update =====

    @Test
    void updateMemory_Success() {
        int memoryId = seeded.memory1().getId();

        MemoryRequestDTO memoryRequestDTO = new MemoryRequestDTO(
                "Updated Memory Title",
                "Updated Subtitle",
                "Updated Content",
                LocalDate.of(2021, 5, 10),
                LocalDate.now(),
                LocalDate.now(),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(memoryRequestDTO)
                .when()
                .put("/memories/" + memoryId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteMemory_Success() {
        int memoryId = seeded.memory1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/memories/" + memoryId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

        // Verify resource deletion
        given()
                .header("Authorization", userToken)
                .when()
                .get("/memories/" + memoryId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createMemory_Unauthorized_WithoutToken() {
        MemoryRequestDTO memoryRequestDTO = new MemoryRequestDTO(
                "Test Title",
                "Test Subtitle",
                "Test Content",
                LocalDate.of(2021, 5, 10),
                LocalDate.now(),
                LocalDate.now(),
                Visibility.PUBLIC
        );

        given()
                .contentType(ContentType.JSON)
                .body(memoryRequestDTO)
                .when()
                .post("/memories")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createMemory_WrongData() {
        MemoryRequestDTO memoryRequestDTO = new MemoryRequestDTO(
                "",
                "",
                "Test Content",
                LocalDate.of(2021, 5, 10),
                LocalDate.now(),
                LocalDate.now(),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(memoryRequestDTO)
                .when()
                .post("/memories")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Read =====

    @Test
    void getMemoryById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/memories/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getMemoryById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/memories/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getMemoryById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/memories/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void getMemoryById_BelongsToAnotherUser() {
        int memoryId = seeded.memory1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/memories/" + memoryId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updateMemory_Unauthorized_WithoutToken() {
        int memoryId = seeded.memory1().getId();

        MemoryRequestDTO memoryRequestDTO = new MemoryRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                "Updated Content",
                LocalDate.of(2021, 5, 10),
                LocalDate.now(),
                LocalDate.now(),
                Visibility.PUBLIC
        );

        given()
                .contentType(ContentType.JSON)
                .body(memoryRequestDTO)
                .when()
                .put("/memories/" + memoryId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void updateMemory_WrongData() {
        MemoryRequestDTO memoryRequestDTO = new MemoryRequestDTO(
                "",
                "",
                "Updated Content",
                LocalDate.of(2021, 5, 10),
                LocalDate.now(),
                LocalDate.now(),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(memoryRequestDTO)
                .when()
                .put("/memories/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void updateMemory_NotFound() {
        MemoryRequestDTO memoryRequestDTO = new MemoryRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                "Updated Content",
                LocalDate.of(2021, 5, 10),
                LocalDate.now(),
                LocalDate.now(),
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(memoryRequestDTO)
                .when()
                .put("/memories/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteMemory_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/memories/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteMemory_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/memories/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deleteMemory_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/memories/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}