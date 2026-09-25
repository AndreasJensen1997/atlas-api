package app.daos;

import app.config.HibernateTestConfig;
import app.daos.userOwned.PersonDAO;
import app.entities.*;
import app.enums.Relation;
import app.enums.Visibility;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
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



    @Test
    void create() {
        User existingUser = seeded.user1();

        Person newPerson = Person.builder().name("Test Person").relation(Relation.FRIEND).user(existingUser).build();

        Person created = personDAO.create(newPerson);
        Person fetched = personDAO.getById(created.getPersonId());

        assertThat(created.getPersonId(), notNullValue());
        assertThat(fetched.getName(), is("Test Person"));
        assertThat(fetched.getRelation(), is(Relation.FRIEND));
        assertThat(fetched.getVisibility(), is(Visibility.PRIVATE));
        assertThat(fetched.getUser(), is(existingUser));
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
            assertThat(p.getUser().getUserId(), is(seeded.user1().getUserId()));
        }
    }

    @Test
    void searchByName() {
        Person seed = seeded.person1();
        String keyword = seed.getName().substring(0, 3).toLowerCase();

        List<Person> results = personDAO.searchByName(keyword);

        assertThat(results, not(empty()));
        assertThat(results, hasItem(seed));
    }

    @Test
    void update() {
        Person seed = seeded.person1();
        User newUser = seeded.user2();

        Person updated = Person.builder()
                .personId(seed.getPersonId())
                .name("Updated Person Name")
                .relation(Relation.MOTHER)
                .user(newUser)
                .build();

        Person result = personDAO.update(updated);

        assertThat(result.getPersonId(), is(seed.getPersonId()));
        assertThat(result.getName(), is("Updated Person Name"));
        assertThat(result.getRelation(), is(Relation.MOTHER));
        assertThat(result.getUpdatedAt(), is(LocalDate.now()));
        assertThat(result.getUser(), is(newUser));
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

    @Test
    void searchByName_withNonExistentKeyword_returnsEmptyList() {
        List<Person> results = personDAO.searchByName("DoesNotExist12345");

        assertThat(results, is(empty()));
    }
}