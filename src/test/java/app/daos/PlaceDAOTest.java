package app.daos;

import app.config.HibernateTestConfig;
import app.entities.*;
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
        seeded = TestPopulator.populate(emf);
        placeDAO = new PlaceDAO(emf);
    }


    @Test
    void create() {
        AppUser existingUser = seeded.user1();

        Place newPlace = Place.builder()
                .name("Test Place")
                .content("Test place description")
                .latitude(55.6761)
                .longitude(12.5683)
                .address("Rådhuspladsen 1")
                .city("Copenhagen")
                .country("Denmark")
                .appUser(existingUser)
                .build();

        Place created = placeDAO.create(newPlace);
        Place fetched = placeDAO.getById(created.getPlaceId());

        assertThat(created.getPlaceId(), notNullValue());
        assertThat(fetched.getName(), is("Test Place"));
        assertThat(fetched.getContent(), is("Test place description"));
        assertThat(fetched.getLatitude(), is(55.6761));
        assertThat(fetched.getLongitude(), is(12.5683));
        assertThat(fetched.getAddress(), is("Rådhuspladsen 1"));
        assertThat(fetched.getCity(), is("Copenhagen"));
        assertThat(fetched.getCountry(), is("Denmark"));
        assertThat(fetched.getAppUser(), is(existingUser));
    }

    @Test
    void getById() {
        Place seed = seeded.place1();
        Place fetched = placeDAO.getById(seed.getPlaceId());
        assertThat(fetched.getPlaceId(), is(seed.getPlaceId()));
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
        List<Place> all = placeDAO.getAllByUserId(seeded.user1().getUserId());

        assertThat(all, not(empty()));
        for (Place p : all) {
            assertThat(p.getAppUser().getUserId(), is(seeded.user1().getUserId()));
        }
    }

    @Test
    void update() {
        Place seed = seeded.place1();
        AppUser newUser = seeded.user2();

        Place updated = Place.builder()
                .placeId(seed.getPlaceId())
                .name("Updated Place Name")
                .content("Updated content")
                .latitude(0.0)
                .longitude(0.0)
                .address("New Address")
                .city("Aarhus")
                .country("Denmark")
                .appUser(newUser)
                .build();

        Place result = placeDAO.update(updated);

        assertThat(result.getPlaceId(), is(seed.getPlaceId()));
        assertThat(result.getName(), is("Updated Place Name"));
        assertThat(result.getContent(), is("Updated content"));
        assertThat(result.getCity(), is("Aarhus"));
        assertThat(result.getAppUser(), is(newUser));
    }

    @Test
    void delete() {
        Place seed = seeded.place1();

        boolean deleted = placeDAO.delete(seed.getPlaceId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> placeDAO.getById(seed.getPlaceId()));
    }

    @Test
    void create_withNullPlace_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullPlace_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Place missing = Place.builder()
                .placeId(999_999)
                .name("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> placeDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}