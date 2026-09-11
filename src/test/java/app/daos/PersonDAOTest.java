package app.daos;

import app.config.HibernateTestConfig;
import app.entities.*;
import app.enums.Relation;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private PersonDAO personDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        seeded = TestPopulator.populate(emf);
        personDAO = new PersonDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        AppUser existingUser = seeded.user1();

        Person newPerson = Person.builder()
                .name("Test Person")
                .relation(Relation.FRIEND)
                .appUser(existingUser)
                .build();

        Person created = personDAO.create(newPerson);
        Person fetched = personDAO.getById(created.getPersonId());

        assertThat(created.getPersonId(), notNullValue());
        assertThat(fetched.getName(), is("Test Person"));
        assertThat(fetched.getRelation(), is(Relation.FRIEND));
        assertThat(fetched.getAppUser(), is(existingUser));
    }

    @Test
    void getById() {
        Person seed = seeded.person1();
        Person fetched = personDAO.getById(seed.getPersonId());
        assertThat(fetched.getPersonId(), is(seed.getPersonId()));
        assertThat(fetched.getName(), is(seed.getName()));
        assertThat(fetched.getRelation(), is(seed.getRelation()));
    }

    @Test
    void getAll() {
        List<Person> all = personDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.person1(), seeded.person2(), seeded.person3()));
    }

    @Test
    void getAllByUserId() {
        List<Person> all = personDAO.getAllByUserId(seeded.user1().getUserId());

        assertThat(all, not(empty()));
        for (Person p : all) {
            assertThat(p.getAppUser().getUserId(), is(seeded.user1().getUserId()));
        }
    }

    @Test
    void update() {
        Person seed = seeded.person1();
        AppUser newUser = seeded.user2();

        Person updated = Person.builder()
                .personId(seed.getPersonId())
                .name("Updated Person Name")
                .relation(Relation.MOTHER)
                .appUser(newUser)
                .build();

        Person result = personDAO.update(updated);

        assertThat(result.getPersonId(), is(seed.getPersonId()));
        assertThat(result.getName(), is("Updated Person Name"));
        assertThat(result.getRelation(), is(Relation.MOTHER));
        assertThat(result.getAppUser(), is(newUser));
    }

    @Test
    void delete() {
        Person seed = seeded.person1();

        boolean deleted = personDAO.delete(seed.getPersonId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> personDAO.getById(seed.getPersonId()));
    }

    @Test
    void create_withNullPerson_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> personDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> personDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> personDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullPerson_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> personDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Person missing = Person.builder()
                .personId(999_999)
                .name("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> personDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> personDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> personDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}