package app.daos;

import app.config.HibernateTestConfig;
import app.entities.*;
import app.exceptions.ApiException;
import app.testUtils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FragmentDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private FragmentDAO fragmentDAO;
    private TestPopulator.SeededData seeded;

    @BeforeEach
    void setUp() {
        seeded = TestPopulator.populate(emf);
        fragmentDAO = new FragmentDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void createAutomaticallyCalculatesWordCountAndDate() {
        AppUser existingUser = seeded.user1();

        Fragment newFragment = Fragment.builder()
                .title("Test note")
                .subtitle("Test note subtitle")
                .content("one two three four five") // 5 words
                .appUser(seeded.user1())
                .build();

        Fragment created = fragmentDAO.create(newFragment);
        Fragment fetched = fragmentDAO.getById(created.getFragmentId());

        assertThat(created.getFragmentId(), notNullValue());
        assertThat(fetched.getTitle(), is("Test note"));
        assertThat(fetched.getSubtitle(), is("Test note subtitle"));
        assertThat(fetched.getContent(), is("one two three four five"));
        assertThat(fetched.getCreatedAt(), is(LocalDate.now()));
        assertThat(fetched.getAppUser(), is(existingUser));
        assertThat(created.getWordCount(), is(5));
    }

    @Test
    void getById() {
        Fragment seed = seeded.fragment1();
        Fragment fetched = fragmentDAO.getById(seed.getFragmentId());
        assertThat(fetched.getFragmentId(), is(seed.getFragmentId()));
        assertThat(fetched.getTitle(), is(seed.getTitle()));
    }

    @Test
    void getAll() {
        List<Fragment> all = fragmentDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.fragment1(), seeded.fragment2(), seeded.fragment3()));
    }

    @Test
    void getAllChaptersByUserId(){

        List<Fragment> all = fragmentDAO.getAllByUserId(seeded.fragment1().getAppUser().getUserId());

        assertThat(all, hasSize(2));
        assertThat(all, containsInAnyOrder(seeded.fragment1(), seeded.fragment2()));
        for (Fragment f : all) {
            assertThat(f.getAppUser().getUserId(), is(seeded.fragment1().getAppUser().getUserId()));
            System.out.println(f.getAppUser().getUserId() + "-" +  seeded.fragment1().getAppUser().getUserId());
        }
    }

    @Test
    void update() {
        Fragment seed = seeded.fragment2();
        AppUser newUser = seeded.user2();

        Fragment updated = Fragment.builder()
                .fragmentId(seed.getFragmentId())
                .title("updated title")
                .subtitle("updated subtitle")
                .content("updated content")
                .createdAt(LocalDate.of(2002,1,1))
                .appUser(newUser)

                .build();

        Fragment result = fragmentDAO.update(updated);

        assertThat(result.getFragmentId(), is(seed.getFragmentId()));
        assertThat(result.getTitle(), is("updated title"));
        assertThat(result.getSubtitle(), is("updated subtitle"));
        assertThat(result.getContent(), is("updated content"));
        assertThat(result.getCreatedAt(), is(LocalDate.of(2002,1,1)));
        assertThat(result.getAppUser(), is(newUser));
        assertThat(result.getWordCount(), is(2));
    }

    @Test
    void delete() {
        Fragment seed = seeded.fragment1();

        boolean deleted = fragmentDAO.delete(seed.getFragmentId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> fragmentDAO.getById(seed.getFragmentId()));
    }

    @Test
    void create_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> fragmentDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> fragmentDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> fragmentDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullStudy_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> fragmentDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Fragment missing = Fragment.builder()
                .fragmentId(999_999)
                .title("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () ->  fragmentDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> fragmentDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> fragmentDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}