package app.testUtils;

import app.entities.*;
import app.enums.Relation;
import app.enums.TargetType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public final class TestPopulator {

    private TestPopulator() {
    }

    public record SeededData(
            AppUser user1, AppUser user2, AppUser user3,
            Chapter chapter1, Chapter chapter2, Chapter chapter3,
            Artifact artifact1, Artifact artifact2, Artifact artifact3,
            ArtifactType musicType, ArtifactType objectType, ArtifactType vehicleType,
            Fragment fragment1, Fragment fragment2, Fragment fragment3,
            EntityList entityList1,
            Memory memory1, Memory memory2, Memory memory3,
            Person person1, Person person2, Person person3,
            Place place1, Place place2, Place place3,
            Story story1,Story story2, Story story3,
            TimeCapsule timeCapsule1,TimeCapsule timeCapsule2, TimeCapsule timeCapsule3,
            Mention mention1, Mention mention2, Mention mention3
    ) {
    }

    public static SeededData populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Clear all tables including the new ones in reverse dependency order
            try {
                em.createNativeQuery("TRUNCATE TABLE mention,timecapsule,story,place,person,memory,entityList,fragment,artifact, artifacttype, chapter, appuser RESTART IDENTITY CASCADE").executeUpdate();
            } catch (PersistenceException e) {
                // Fallback if tables don't exist yet
            }

            // Create baseline users
            AppUser user1 = AppUser.builder().name("Andreas").email("andreas.jensen@outlook.dk").password("1234").build();
            AppUser user2 = AppUser.builder().name("morten").email("morten.jensen@outlook.dk").password("1234").build();
            AppUser user3 = AppUser.builder().name("theis").email("theis.jensen@outlook.dk").password("1234").build();

            em.persist(user1);
            em.persist(user2);
            em.persist(user3);

            // Create chapters
            Chapter chapter1 = Chapter.builder().title("Years in china ").subtitle("My exchange years in china").appUser(user1).build();
            Chapter chapter2 = Chapter.builder().title("Early life ").subtitle("from crawling to walking").appUser(user2).build();
            Chapter chapter3 = Chapter.builder().title("University").subtitle("how i spent my 3 years at uni").appUser(user3).build();

            em.persist(chapter1);
            em.persist(chapter2);
            em.persist(chapter3);

            // Create and persist Artifact Types first (since Artifacts depend on them)
            ArtifactType musicType = ArtifactType.builder().typeName("Music").build();
            ArtifactType objectType = ArtifactType.builder().typeName("Physical Object").build();
            ArtifactType vehicleType = ArtifactType.builder().typeName("Vehicle").build();

            em.persist(musicType);
            em.persist(objectType);
            em.persist(vehicleType);

            // Create and persist Artifacts
            Artifact artifact1 = Artifact.builder().title("I forget where we were").subtitle("ben howard album").content("my favourite album").createdAt(LocalDate.of(2026, 1, 1)).appUser(user1).artifactType(musicType).build();
            Artifact artifact2 = Artifact.builder().title("Magnus the teddy").subtitle("childhood teddy").content("my favourite teddy as a kid").createdAt(LocalDate.of(2022, 1, 1)).appUser(user2).artifactType(objectType).build();
            Artifact artifact3 = Artifact.builder().title("Red bike").subtitle("My first bike").content("my mom got me this for my third birthday").createdAt(LocalDate.of(2002, 1, 1)).appUser(user3).artifactType(vehicleType).build();

            em.persist(artifact1);
            em.persist(artifact2);
            em.persist(artifact3);

            // FRAGMENTS
            Fragment fragment1 = Fragment.builder().title("idea for wedding speech").subtitle("Daniels wedding").content("talk about vacation in sweden").createdAt(LocalDate.of(2026, 1, 1)).appUser(user1).build();
            Fragment fragment2 = Fragment.builder().title("book title idea").subtitle("book project").content("in the beginning").createdAt(LocalDate.of(2026, 3, 3)).appUser(user1).build();
            Fragment fragment3 = Fragment.builder().title("dinner with jamie").subtitle("dinner date").content("remember to buy tomatoes").createdAt(LocalDate.of(2026, 4, 5)).appUser(user2).build();

            em.persist(fragment1);
            em.persist(fragment2);
            em.persist(fragment3);

            EntityList entityList1 = EntityList.builder().title("top movies").subtitle("my favourite work of arts").appUser(user1).build();
            entityList1.addItem(EntityListItem.builder().text("Interstellar").build());
            entityList1.addItem(EntityListItem.builder().text("The Matrix").build());
            entityList1.addItem(EntityListItem.builder().text("odyssey").build());

            em.persist(entityList1);


            // MEMORIES

            Memory memory1 = Memory.builder().title("wedding night").subTitle("best moments from wedding night").content("The night was magic").appUser(user1).build();
            Memory memory2 = Memory.builder().title("graduation day").subTitle("The night we finised").content("The night was magic").appUser(user2).build();
            Memory memory3 = Memory.builder().title("surgery").subTitle("hip surgery").content("the day we fixed my issue").appUser(user3).build();

            em.persist(memory1);
            em.persist(memory2);
            em.persist(memory3);


            Person person1 = Person.builder().name("jimmi").relation(Relation.FATHER).appUser(user1).build();
            Person person2 = Person.builder().name("trine").relation(Relation.MOTHER).appUser(user2).build();
            Person person3 = Person.builder().name("daniel").relation(Relation.FRIEND).appUser(user3).build();

            em.persist(person1);
            em.persist(person2);
            em.persist(person3);

            // Inside your TestPopulator.populate() method:

            Place place1 = Place.builder().name("Copenhagen Central").content("Main station area").latitude(55.6761).longitude(12.5683).address("Bernstorffsgade 16").city("Copenhagen").country("Denmark").appUser(user1).build();
            Place place2 = Place.builder().name("Aarhus Ø").content("Modern harbor front").latitude(56.1629).longitude(10.2039).address("Ankersgade 1").city("Aarhus").country("Denmark").appUser(user2).build();
            Place place3 = Place.builder().name("Odense Zoo").content("Family attraction").latitude(55.3852).longitude(10.3736).address("Sdr. Boulevard 306").city("Odense").country("Denmark").appUser(user3).build();

            em.persist(place1);
            em.persist(place2);
            em.persist(place3);

            Story story1 = Story.builder().title("First Story").subTitle("Beginning").content("Content of the first story...").startDate(LocalDate.of(2026, 1, 1)).endDate(LocalDate.of(2026, 1, 3)).build();
            Story story2 = Story.builder().title("Second Story").subTitle("Middle").content("Content of the second story...").startDate(LocalDate.of(2026, 1, 4)).endDate(LocalDate.of(2026, 1, 6)).build();
            Story story3 = Story.builder().title("Third Story").subTitle("End").content("Content of the third story...").startDate(LocalDate.of(2026, 1, 7)).endDate(LocalDate.of(2026, 1, 10)).build();

            em.persist(story1);
            em.persist(story2);
            em.persist(story3);

            TimeCapsule timeCapsule1 = TimeCapsule.builder().content("Memory from 2024").unlockDate(LocalDate.of(2029,1,1)).lockStatus(false).appUser(user1).build();
            TimeCapsule timeCapsule2 = TimeCapsule.builder().content("Open in 2030").unlockDate(LocalDate.of(2023,1,1)).lockStatus(true).appUser(user2).build();
            TimeCapsule timeCapsule3 = TimeCapsule.builder().content("Open in 2050").unlockDate(LocalDate.of(2023,1,1)).lockStatus(true).appUser(user3).build();

            em.persist(timeCapsule1);
            em.persist(timeCapsule2);
            em.persist(timeCapsule3);

            Mention mention1 = Mention.builder().targetType(TargetType.PERSON).targetId(person1.getPersonId()). ownerType(TargetType.MEMORY).ownerId(memory1.getMemoryId()).build();
            Mention mention2 = Mention.builder().targetType(TargetType.PLACE).targetId(place1.getPlaceId()).ownerType(TargetType.STORY).ownerId(story1.getStoryId()).build();
            Mention mention3 = Mention.builder().targetType(TargetType.PERSON).targetId(person2.getPersonId()).ownerType(TargetType.CHAPTER).ownerId(chapter1.getChapterId()).build();

            em.persist(mention1);
            em.persist(mention2);
            em.persist(mention3);


            em.getTransaction().commit();

            return new SeededData(user1, user2, user3,
                    chapter1, chapter2, chapter3,
                    artifact1, artifact2, artifact3,
                    musicType, objectType, vehicleType,
                    fragment1, fragment2, fragment3,
                    entityList1,
                    memory1, memory2, memory3,
                    person1, person2, person3,
                    place1,place2,place3,story1,story2,story3,
                    timeCapsule1,timeCapsule2,timeCapsule3,
                    mention1, mention2, mention3);
        }
    }
}