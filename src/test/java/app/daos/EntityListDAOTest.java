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
class EntityListDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private EntityListDAO entityListDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        seeded = TestPopulator.populate(emf);
        entityListDAO = new EntityListDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void createAutomaticallyCalculatesItemCountAndDate() {

        AppUser existingUser = seeded.user1();

        EntityList newEntityList = EntityList.builder().title("Test entityList").subtitle("Test entityList subtitle").appUser(existingUser).build();

        newEntityList.addItem(EntityListItem.builder().text("Item 1").build());
        newEntityList.addItem(EntityListItem.builder().text("Item 2").build());
        newEntityList.addItem(EntityListItem.builder().text("Item 3").build());

        EntityList created = entityListDAO.create(newEntityList);

        EntityList fetched = entityListDAO.getById(created.getListId());

        assertThat(created.getListId(), notNullValue());
        assertThat(fetched.getTitle(), is("Test entityList"));
        assertThat(fetched.getSubtitle(), is("Test entityList subtitle"));
        assertThat(fetched.getItems().size(), is(3));
        assertThat(fetched.getCreatedAt(), is(LocalDate.now()));
        assertThat(fetched.getAppUser(), is(existingUser));
    }

    @Test
    void getById() {
        EntityList seed = seeded.entityList1();
        EntityList fetched = entityListDAO.getById(seed.getListId());
        assertThat(fetched.getListId(), is(seed.getListId()));
        assertThat(fetched.getTitle(), is(seed.getTitle()));
    }

    @Test
    void getAll() {
        List<EntityList> all = entityListDAO.getAll();
        assertThat (all, hasSize(1));
        assertThat(all, contains(seeded.entityList1()));
    }

    @Test
    void getAllChaptersByUserId(){

        List<EntityList> all = entityListDAO.getAllByUserId(seeded.entityList1().getAppUser().getUserId());

        assertThat(all, hasSize(1));
        assertThat(all, containsInAnyOrder(seeded.entityList1()));
        for (EntityList e : all) {
            assertThat(e.getAppUser().getUserId(), is(seeded.entityList1().getAppUser().getUserId()));
        }
    }

    @Test
    void update() {
        EntityList seed = seeded.entityList1();
        AppUser newUser = seeded.user2();

        EntityList updated = EntityList.builder()
                .listId(seed.getListId())
                .title("updated title")
                .subtitle("updated subtitle")
                .appUser(newUser)
                .build();

        EntityList result = entityListDAO.update(updated);

        assertThat(result.getListId(), is(seed.getListId()));
        assertThat(result.getTitle(), is("updated title"));
        assertThat(result.getSubtitle(), is("updated subtitle"));
        assertThat(result.getAppUser(), is(newUser));
    }

    @Test
    void delete() {
        EntityList seed = seeded.entityList1();

        boolean deleted = entityListDAO.delete(seed.getListId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> entityListDAO.getById(seed.getListId()));
    }

    @Test
    void create_withNullEntityList_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> entityListDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> entityListDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> entityListDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullEntityList_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> entityListDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        EntityList missing = EntityList.builder()
                .listId(999_999)
                .title("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> entityListDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> entityListDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> entityListDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}