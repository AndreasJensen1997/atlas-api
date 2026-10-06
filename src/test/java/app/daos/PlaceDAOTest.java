package app.daos;

import app.config.HibernateTestConfig;
import app.daos.userOwned.PlaceDAO;
import app.entities.*;
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
class PlaceDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private PlaceDAO placeDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        TestPopulator.cleanup(emf);
        seeded = TestPopulator.populate(emf);
        placeDAO = new PlaceDAO(emf);
    }


    @Test
    void create() {
        User existingUser = seeded.user1();

        Place newPlace = Place.builder()
                .name("Test Place")
                .content("Test place description")
                .latitude(55.6761)
                .longitude(12.5683)
                .address("Rådhuspladsen 1")
                .city("Copenhagen")
                .country("Denmark")
                .user(existingUser)
                .build();

        Place created = placeDAO.create(newPlace);
        Place fetched = placeDAO.getById(created.getId());

        assertThat(created.getId(), notNullValue());
        assertThat(fetched.getName(), is("Test Place"));
        assertThat(fetched.getContent(), is("Test place description"));
        assertThat(fetched.getLatitude(), is(55.6761));
        assertThat(fetched.getLongitude(), is(12.5683));
        assertThat(fetched.getAddress(), is("Rådhuspladsen 1"));
        assertThat(fetched.getVisibility(), is(Visibility.PRIVATE));
        assertThat(fetched.getCity(), is("Copenhagen"));
        assertThat(fetched.getCountry(), is("Denmark"));
        assertThat(fetched.getUser(), is(existingUser));
    }

    @Test
    void getById() {
        Place seed = seeded.place1();
        Place fetched = placeDAO.getById(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getName(), is(seed.getName()));
        assertThat(fetched.getCity(), is(seed.getCity()));
    }

    @Test
    void getAll() {
        List<Place> all = placeDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.place1(), seeded.place2(), seeded.place3()));
    }

    @Test
    void getAllByUserId() {
        List<Place> all = placeDAO.getAllByUserId(seeded.user1().getId());

        assertThat(all, not(empty()));
        for (Place p : all) {
            assertThat(p.getUser().getId(), is(seeded.user1().getId()));
        }
    }

    @Test
    void searchByName() {
        Place seed = seeded.place1();
        String keyword = seed.getName().substring(0, 3).toLowerCase();

        List<Place> results = placeDAO.searchByTitle(keyword,seed.getUser().getId());

        assertThat(results, not(empty()));
        assertThat(results, hasItem(seed));
    }

    @Test
    void update() {
        Place seed = seeded.place1();
        User newUser = seeded.user2();

        Place updated = Place.builder()
                .id(seed.getId())
                .name("Updated Place Name")
                .content("Updated content")
                .latitude(0.0)
                .longitude(0.0)
                .address("New Address")
                .city("Aarhus")
                .country("Denmark")
                .user(newUser)
                .build();

        Place result = placeDAO.update(updated);

        assertThat(result.getId(), is(seed.getId()));
        assertThat(result.getName(), is("Updated Place Name"));
        assertThat(result.getContent(), is("Updated content"));
        assertThat(result.getCity(), is("Aarhus"));
        assertThat(result.getUpdatedAt(), is(LocalDate.now()));
        assertThat(result.getUser(), is(newUser));
    }

    @Test
    void delete() {
        Place seed = seeded.place1();

        boolean deleted = placeDAO.delete(seed.getId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> placeDAO.getById(seed.getId()));
    }

    @Test
    void create_withNullPlace_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.create(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.getById(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.getById(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void update_withNullPlace_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.update(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Place missing = Place.builder().id(999_999).name("Missing").build();

        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.update(missing));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.delete(null));
        assertThat(ex.getStatusCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.delete(999_999));
        assertThat(ex.getStatusCode(), is(404));
    }

    @Test
    void searchByName_withNonExistentKeyword_returnsEmptyList() {
        List<Place> results = placeDAO.searchByTitle("DoesNotExist12345",999);

        assertThat(results, is(empty()));
    }
}