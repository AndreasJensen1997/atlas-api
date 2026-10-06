package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.person.PersonRequestDTO;
import app.dtos.person.PersonResponseDTO;
import app.enums.Relation;
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
public class PersonEndpointTest {

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
    void createPerson_Success() {
        PersonRequestDTO personRequestDTO = new PersonRequestDTO(
                "Jane Doe",
                "Close friend from university",
                Relation.FATHER,
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(personRequestDTO)
                .when()
                .post("/persons")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("id", notNullValue())
                .body("name", equalTo("Jane Doe"));
    }

    // ===== Read =====

    @Test
    void getPersonById_Success() {
        int personId = seeded.person1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .get("/persons/" + personId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(personId))
                .body("name", equalTo(seeded.person1().getName()));
    }

    @Test
    void getAllPersons_Success() {
        List<PersonResponseDTO> persons = given()
                .header("Authorization", userToken)
                .when()
                .get("/persons")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .log().body()
                .body("size()", is(1))
                .body("[0].name", equalTo(seeded.person1().getName()))
                .extract()
                .jsonPath()
                .getList("", PersonResponseDTO.class);
    }

    @Test
    void getRandomPerson_Success() {
        List<String> expectedNames = List.of(
                seeded.person1().getName(),
                seeded.person2().getName()
        );

        given()
                .header("Authorization", userToken)
                .when()
                .get("/persons/random")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("name", in(expectedNames));
    }

    // ===== Update =====

    @Test
    void updatePerson_Success() {
        int personId = seeded.person1().getId();

        PersonRequestDTO personRequestDTO = new PersonRequestDTO(
                "Updated Person Name",
                "Updated Content Description",
                Relation.MOTHER,
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(personRequestDTO)
                .when()
                .put("/persons/" + personId)
                .then()
                .statusCode(HttpStatus.OK.getCode());
    }

    // ===== Delete =====

    @Test
    void deletePerson_Success() {
        int personId = seeded.person1().getId();

        given()
                .header("Authorization", userToken)
                .when()
                .delete("/persons/" + personId)
                .then()
                .statusCode(HttpStatus.OK.getCode());

        // Verify resource deletion
        given()
                .header("Authorization", userToken)
                .when()
                .get("/persons/" + personId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ==========================================
    // ENDPOINT TESTS FAILURE
    // ==========================================

    // ===== Create =====

    @Test
    void createPerson_Unauthorized_WithoutToken() {
        PersonRequestDTO personRequestDTO = new PersonRequestDTO(
                "Test Name",
                "Test Content",
                Relation.ADOPTIVE_CHILD,
                Visibility.PUBLIC
        );

        given()
                .contentType(ContentType.JSON)
                .body(personRequestDTO)
                .when()
                .post("/persons")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void createPerson_WrongData() {
        PersonRequestDTO personRequestDTO = new PersonRequestDTO(
                "",
                "Test Content",
                null,
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(personRequestDTO)
                .when()
                .post("/persons")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    // ===== Read =====

    @Test
    void getPersonById_Unauthorized_WithoutToken() {
        given()
                .when()
                .get("/persons/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void getPersonById_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/persons/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void getPersonById_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .get("/persons/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void getPersonById_BelongsToAnotherUser() {
        int personId = seeded.person1().getId();
        String wrongToken = "Bearer " + JWTToken.generateToken(seeded.user2().getEmail());

        given()
                .header("Authorization", wrongToken)
                .when()
                .get("/persons/" + personId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Update =====

    @Test
    void updatePerson_Unauthorized_WithoutToken() {
        int personId = seeded.person1().getId();

        PersonRequestDTO personRequestDTO = new PersonRequestDTO(
                "Updated Name",
                "Updated Content",
                Relation.MOTHER,
                Visibility.PUBLIC
        );

        given()
                .contentType(ContentType.JSON)
                .body(personRequestDTO)
                .when()
                .put("/persons/" + personId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void updatePerson_WrongData() {
        PersonRequestDTO personRequestDTO = new PersonRequestDTO(
                "",
                "Updated Content",
                null,
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(personRequestDTO)
                .when()
                .put("/persons/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void updatePerson_NotFound() {
        PersonRequestDTO personRequestDTO = new PersonRequestDTO(
                "Updated Name",
                "Updated Content",
                Relation.AUNT,
                Visibility.PUBLIC
        );

        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(personRequestDTO)
                .when()
                .put("/persons/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }

    // ===== Delete =====

    @Test
    void deletePerson_Unauthorized_WithoutToken() {
        given()
                .when()
                .delete("/persons/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void deletePerson_WrongData() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/persons/-1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    void deletePerson_NotFound() {
        given()
                .header("Authorization", userToken)
                .when()
                .delete("/persons/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
    }
}