package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.entityList.EntityListRequestDTO;
import app.dtos.entityList.EntityListResponseDTO;
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
public class EntityListEndpointTest {

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
    void createEntityList_Success() {
        EntityListRequestDTO requestDTO = new EntityListRequestDTO(
                "My Reading List",
                "Books to read",
                Visibility.PUBLIC,
                List.of("Clean Code", "Pragmatic Programmer")
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/entity-lists")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("title", equalTo("My Reading List"));
    }

    // ===== Read =====

    @Test
    void getEntityListById_Success() {
        int listId = seeded.entityList1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/entity-lists/" + listId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(listId))
                .body("title", equalTo(seeded.entityList1().getTitle()));
    }

    @Test
    void getAllEntityLists_Success() {
        List<EntityListResponseDTO> lists = given()
                .header("Authorization", userToken)
                .when()
                .get("/entity-lists")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(1))
                .body("[0].title", equalTo(seeded.entityList1().getTitle()))
                .extract()
                .jsonPath()
                .getList("", EntityListResponseDTO.class);
    }

    // ===== Update =====

    @Test
    void updateEntityList_Success() {
        int listId = seeded.entityList1().getId();

        EntityListRequestDTO requestDTO = new EntityListRequestDTO(
                "Updated List Title",
                "Updated Subtitle",
                Visibility.PUBLIC,
                List.of("Updated Item 1", "Updated Item 2")
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/entity-lists/" + listId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteEntityList_Success() {
        int listId = seeded.entityList1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/entity-lists/" + listId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

        // Verify resource deletion
        given()
                .header("Authorization", userToken)
                .when()
                .get("/entity-lists/" + listId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createEntityList_Unauthorized_WithoutToken() {
        EntityListRequestDTO requestDTO = new EntityListRequestDTO(
                "Test Title",
                "Test Subtitle",
                Visibility.PUBLIC,
                List.of("Item 1")
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/entity-lists")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createEntityList_WrongData() {
        EntityListRequestDTO requestDTO = new EntityListRequestDTO(
                "",
                "Test Subtitle",
                Visibility.PUBLIC,
                List.of()
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/entity-lists")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Read =====

    @Test
    void getEntityListById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/entity-lists/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getEntityListById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/entity-lists/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getEntityListById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/entity-lists/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void getEntityListById_BelongsToAnotherUser() {
        int listId = seeded.entityList1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/entity-lists/" + listId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updateEntityList_Unauthorized_WithoutToken() {
        int listId = seeded.entityList1().getId();

        EntityListRequestDTO requestDTO = new EntityListRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                Visibility.PUBLIC,
                List.of("Updated Item")
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/entity-lists/" + listId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void updateEntityList_WrongData() {
        EntityListRequestDTO requestDTO = new EntityListRequestDTO(
                "",
                "Updated Subtitle",
                Visibility.PUBLIC,
                List.of()
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/entity-lists/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void updateEntityList_NotFound() {
        EntityListRequestDTO requestDTO = new EntityListRequestDTO(
                "Updated Title",
                "Updated Subtitle",
                Visibility.PUBLIC,
                List.of("Updated Item")
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/entity-lists/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteEntityList_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/entity-lists/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteEntityList_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/entity-lists/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deleteEntityList_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/entity-lists/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}