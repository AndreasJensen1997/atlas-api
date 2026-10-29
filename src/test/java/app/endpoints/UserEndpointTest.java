package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.user.LoginRequestDTO;
import app.dtos.user.RegisterRequestDTO;
import app.dtos.user.UserResponseDTO;
import app.dtos.user.UserUpdateDTO;
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
public class UserEndpointTest {

    private static Javalin app;
    private static EntityManagerFactory emf;
    private static ApplicationConfig applicationConfig;

    private TestPopulator.SeededData seeded;
    private String userToken;
    private String adminToken;

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
        RestAssured.basePath = "/api/v1";
    }

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);

        userToken = "Bearer " + JWTToken.generateToken(
                seeded.user1().getId()
        );
        adminToken = "Bearer " + JWTToken.generateToken(
                seeded.admin().getId()
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


    @Test
    void register_and_login_AreCaseInsensitiveOnEmail() {
        RegisterRequestDTO register = new RegisterRequestDTO("Test User", "Test.User@Example.dk", "Password1!", "Password1!");

        given()
                .contentType(ContentType.JSON).body(register)
                .when().post("/auth/register")
                .then().statusCode(HttpStatus.CREATED.getCode());

        // Same mail with caps lock. no dupes
        RegisterRequestDTO duplicate = new RegisterRequestDTO("Other", "TEST.USER@EXAMPLE.DK", "Password1!", "Password1!");

        given()
                .contentType(ContentType.JSON).body(duplicate)
                .when().post("/auth/register")
                .then().statusCode(HttpStatus.CONFLICT.getCode());

        // Login caps lock
        LoginRequestDTO login = new LoginRequestDTO("TEST.USER@EXAMPLE.DK", "Password1!");

        given()
                .contentType(ContentType.JSON).body(login)
                .when().post("/auth/login")
                .then().statusCode(HttpStatus.OK.getCode())
                .body("token", notNullValue());
    }




    // ===== Read =====

    @Test
    void getCurrentUser_Success() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/users/current")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(seeded.user1().getId()))
                .body("email", equalTo(seeded.user1().getEmail()));
    }

    @Test
    void getUserById_Success() {
        int userId = seeded.user1().getId();

        given()
                .header("Authorization", adminToken)
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(userId))
                .body("email", equalTo(seeded.user1().getEmail()));
    }

    @Test
    void getAllUsers_Success() {
        List<UserResponseDTO> users = given()
                .header("Authorization", adminToken)
                .when()
                .get("/users")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", greaterThanOrEqualTo(1))
                .extract()
                .jsonPath()
                .getList("", UserResponseDTO.class);
    }

    // ===== Update =====

    @Test
    void updateUser_Success() {
        int userId = seeded.user1().getId();

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "Updated Name",
                "updated.email@test.com"
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/users/" + userId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(userId))
                .body("name", equalTo("Updated Name"))
                .body("email", equalTo("updated.email@test.com"));
    }

    @Test
    void changeEmail_TokenStillWorks() {
        UserUpdateDTO dto = new UserUpdateDTO("Andreas", "new.mail@outlook.dk");

        given().header("Authorization", userToken).contentType(ContentType.JSON).body(dto)
                .when().put("/users/" + seeded.user1().getId())
                .then().statusCode(HttpStatus.OK.getCode());

        given().header("Authorization", userToken)                 // samme token som før emailskiftet
                .when().get("/users/current")
                .then().statusCode(HttpStatus.OK.getCode())
                .body("email", equalTo("new.mail@outlook.dk"));
    }


    // ===== Delete =====

    @Test
    void deleteUser_Success() {
        int userId = seeded.user1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/users/" + userId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.getCode()); // 204

        // Verify user was deleted
        given()
                .header("Authorization", adminToken )
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Read =====

    @Test
    void getCurrentUser_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/users/current")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getUserById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getUserById_WrongData() {
        given()
                .header("Authorization", adminToken)
                .when()
                .get("/users/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getUserById_NotFound() {
        given()
                .header("Authorization", adminToken)
                .when()
                .get("/users/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updateUser_Unauthorized_WithoutToken() {
        int userId = seeded.user1().getId();

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "Updated Name",
                "updated.email@test.com"
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/users/" + userId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void changeEmail_AlreadyInUse_Conflict() {
        UserUpdateDTO dto = new UserUpdateDTO("Andreas", seeded.user2().getEmail());

        given().header("Authorization", userToken).contentType(ContentType.JSON).body(dto)
                .when().put("/users/" + seeded.user1().getId())
                .then().statusCode(HttpStatus.CONFLICT.getCode());
    }

    @Test
    void updateUser_BelongsToAnotherUser() {
        int targetUserId = seeded.user1().getId();
        // user2 attempting to update user1's account
        String wrongUserToken = "Bearer " + JWTToken.generateToken(seeded.user2().getId());

        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "Hacked Name",
                "hacked@test.com"
        );

        given()
                .header("Authorization", wrongUserToken)
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/users/" + targetUserId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode()); // Throws IllegalArgumentException: "You can only update your own account."
    }

    @Test
    void updateUser_WrongData() {
        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "",
                "invalid-email"
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/users/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Delete =====

    @Test
    void deleteUser_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/users/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deleteUser_BelongsToAnotherUser() {
        int targetUserId = seeded.user1().getId();
        // user2 attempting to delete user1's account
        String wrongUserToken = "Bearer " + JWTToken.generateToken(seeded.user2().getId());

        given()
                .header("Authorization", wrongUserToken)
                .when()
                .delete("/users/" + targetUserId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode()); // Throws IllegalArgumentException: "You can only delete your own account."
    }

    @Test
    void deleteUser_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/users/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }
}