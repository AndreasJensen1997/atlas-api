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
class MemoryDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private MemoryDAO memoryDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        seeded = TestPopulator.populate(emf);
        memoryDAO = new MemoryDAO(emf);
    }



    @Test
    void create() {
        AppUser existingUser = seeded.user1();

        Memory newMemory = Memory.builder()
                .title("Test Memory")
                .content("Test memory content")
                .appUser(existingUser)
                .build();

        Memory created = memoryDAO.create(newMemory);
        Memory fetched = memoryDAO.getById(created.getMemoryId());

        assertThat(created.getMemoryId(), notNullValue());
        assertThat(fetched.getTitle(), is("Test Memory"));
        assertThat(fetched.getContent(), is("Test memory content"));
        assertThat(fetched.getAppUser(), is(existingUser));
    }

    @Test
    void getById() {
        Memory seed = seeded.memory1();
        Memory fetched = memoryDAO.getById(seed.getMemoryId());
        assertThat(fetched.getMemoryId(), is(seed.getMemoryId()));
        assertThat(fetched.getTitle(), is(seed.getTitle()));
    }

    @Test
    void getAll() {
        List<Memory> all = memoryDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.memory1(), seeded.memory2(), seeded.memory3()));
    }

    @Test
    void getAllByUserId() {
        List<Memory> all = memoryDAO.getAllByUserId(seeded.user1().getUserId());

        assertThat(all, not(empty()));
        for (Memory m : all) {
            assertThat(m.getAppUser().getUserId(), is(seeded.user1().getUserId()));
        }
    }

    @Test
    void searchByName_withValidKeyword_returnsMatchingItems() {
        Memory seed = seeded.memory1();
        String keyword = seed.getTitle().substring(0, 3).toLowerCase(); // Test partial and case-insensitive match

        List<Memory> results = memoryDAO.searchByName(keyword);

        assertThat(results, not(empty()));
        assertThat(results, hasItem(seed));
    }

    @Test
    void searchByName_withNonExistentKeyword_returnsEmptyList() {
        List<Memory> results = memoryDAO.searchByName("DoesNotExist12345");

        assertThat(results, is(empty()));
    }

    @Test
    void update() {
        Memory seed = seeded.memory1();
        AppUser newUser = seeded.user2();

        Memory updated = Memory.builder()
                .memoryId(seed.getMemoryId())
                .title("Updated memory title")
                .content("Updated memory content")
                .appUser(newUser)
                .build();

        Memory result = memoryDAO.update(updated);

        assertThat(result.getMemoryId(), is(seed.getMemoryId()));
        assertThat(result.getTitle(), is("Updated memory title"));
        assertThat(result.getContent(), is("Updated memory content"));
        assertThat(result.getAppUser(), is(newUser));
    }

    @Test
    void delete() {
        Memory seed = seeded.memory1();

        boolean deleted = memoryDAO.delete(seed.getMemoryId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> memoryDAO.getById(seed.getMemoryId()));
    }

    @Test
    void create_withNullMemory_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> memoryDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> memoryDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> memoryDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullMemory_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> memoryDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Memory missing = Memory.builder()
                .memoryId(999_999)
                .title("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> memoryDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> memoryDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> memoryDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}