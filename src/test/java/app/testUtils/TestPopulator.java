package app.testUtils;

import app.entities.*;
import app.enums.NotificationType;
import app.enums.Relation;
import app.enums.Role;
import app.enums.TargetType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.time.LocalDate;

public final class TestPopulator {

    private TestPopulator() {
    }

    public record SeededData(
            User user1, User user2, User user3,
            Chapter chapter1, Chapter chapter2, Chapter chapter3,
            Artifact artifact1, Artifact artifact2, Artifact artifact3, Artifact artifact4,
            ArtifactType musicType, ArtifactType objectType, ArtifactType vehicleType,
            Fragment fragment1, Fragment fragment2, Fragment fragment3,
            EntityList entityList1,
            Memory memory1, Memory memory2, Memory memory3,
            Person person1, Person person2, Person person3,
            Place place1, Place place2, Place place3,
            Story story1, Story story2, Story story3,
            TimeCapsule timeCapsule1, TimeCapsule timeCapsule2, TimeCapsule timeCapsule3,
            Mention mention1, Mention mention2, Mention mention3,
            Devlog devlog1, Devlog devlog2, Devlog devlog3,
            Notification notification1, Notification notification2, Notification notification3,
            GeminiPrompt geminiPrompt1, GeminiPrompt geminiPrompt2, GeminiPrompt geminiPrompt3
    ) {
    }

    // Standalone cleanup method for resetting the database
    public static void cleanup(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.createNativeQuery("TRUNCATE TABLE GeminiPrompt,devlog,mention, timecapsule, story, place, person, memory, entityList, fragment, artifact, artifacttype, chapter, users RESTART IDENTITY CASCADE").executeUpdate();
            } catch (PersistenceException e) {
                // Ignores error if tables don't exist yet on first boot
            }
            em.getTransaction().commit();
        }
    }

    public static SeededData populate(EntityManagerFactory emf) {
        cleanup(emf);

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create baseline users
            User user1 = User.builder().name("Andreas").email("andreas.jensen@outlook.dk").password("1234").role(Role.USER).build();
            User user2 = User.builder().name("morten").email("morten.jensen@outlook.dk").password("1234").role(Role.USER).build();
            User user3 = User.builder().name("theis").email("theis.jensen@outlook.dk").password("1234").role(Role.USER).build();

            em.persist(user1);
            em.persist(user2);
            em.persist(user3);

            // Create chapters
            Chapter chapter1 = Chapter.builder().title("Years in china ").subtitle("My exchange years in china").user(user1).build();
            Chapter chapter2 = Chapter.builder().title("Early life ").subtitle("from crawling to walking").user(user2).build();
            Chapter chapter3 = Chapter.builder().title("University").subtitle("how i spent my 3 years at uni").user(user3).build();

            em.persist(chapter1);
            em.persist(chapter2);
            em.persist(chapter3);

            // Create and persist Artifact Types first (since Artifacts depend on them)
            ArtifactType musicType = ArtifactType.builder().name("Music").user(user1).build();
            ArtifactType objectType = ArtifactType.builder().name("Physical Object").user(user2).build();
            ArtifactType vehicleType = ArtifactType.builder().name("Vehicle").user(user3).build();

            em.persist(musicType);
            em.persist(objectType);
            em.persist(vehicleType);

            // Create and persist Artifacts
            Artifact artifact1 = Artifact.builder().title("I forget where we were").subtitle("ben howard album").content("my favourite album").createdAt(LocalDate.of(2026, 1, 1)).user(user1).artifactType(musicType).build();
            Artifact artifact2 = Artifact.builder().title("Magnus the teddy").subtitle("childhood teddy").content("my favourite teddy as a kid").createdAt(LocalDate.of(2022, 1, 1)).user(user2).artifactType(objectType).build();
            Artifact artifact3 = Artifact.builder().title("Red bike").subtitle("My first bike").content("my mom got me this for my third birthday").createdAt(LocalDate.of(2002, 1, 1)).user(user3).artifactType(vehicleType).build();
            Artifact artifact4 = Artifact.builder().title("My first car").subtitle("car").content("first car").createdAt(LocalDate.of(2022, 1, 1)).user(user2).artifactType(objectType).build();

            em.persist(artifact1);
            em.persist(artifact2);
            em.persist(artifact3);
            em.persist(artifact4);

            // FRAGMENTS
            Fragment fragment1 = Fragment.builder().title("idea for wedding speech").subtitle("Daniels wedding").content("talk about vacation in sweden").createdAt(LocalDate.of(2026, 1, 1)).user(user1).build();
            Fragment fragment2 = Fragment.builder().title("book title idea").subtitle("book project").content("in the beginning").createdAt(LocalDate.of(2026, 3, 3)).user(user1).build();
            Fragment fragment3 = Fragment.builder().title("dinner with jamie").subtitle("dinner date").content("remember to buy tomatoes").createdAt(LocalDate.of(2026, 4, 5)).user(user2).build();

            em.persist(fragment1);
            em.persist(fragment2);
            em.persist(fragment3);

            EntityList entityList1 = EntityList.builder().title("top movies").subtitle("my favourite work of arts").user(user1).build();
            entityList1.addItem(EntityListItem.builder().text("Interstellar").build());
            entityList1.addItem(EntityListItem.builder().text("The Matrix").build());
            entityList1.addItem(EntityListItem.builder().text("odyssey").build());

            em.persist(entityList1);


            // MEMORIES

            Memory memory1 = Memory.builder().title("wedding night").subtitle("best moments from wedding night").content("The night was magic").user(user1).build();
            Memory memory2 = Memory.builder().title("graduation day").subtitle("The night we finised").content("The night was magic").user(user2).build();
            Memory memory3 = Memory.builder().title("surgery").subtitle("hip surgery").content("the day we fixed my issue").user(user3).build();

            em.persist(memory1);
            em.persist(memory2);
            em.persist(memory3);


            Person person1 = Person.builder().name("jimmi").relation(Relation.FATHER).user(user1).build();
            Person person2 = Person.builder().name("trine").relation(Relation.MOTHER).user(user2).build();
            Person person3 = Person.builder().name("daniel").relation(Relation.FRIEND).user(user3).build();

            em.persist(person1);
            em.persist(person2);
            em.persist(person3);

            Place place1 = Place.builder().name("Copenhagen Central").content("Main station area").latitude(55.6761).longitude(12.5683).address("Bernstorffsgade 16").city("Copenhagen").country("Denmark").user(user1).build();
            Place place2 = Place.builder().name("Aarhus Ø").content("Modern harbor front").latitude(56.1629).longitude(10.2039).address("Ankersgade 1").city("Aarhus").country("Denmark").user(user2).build();
            Place place3 = Place.builder().name("Odense Zoo").content("Family attraction").latitude(55.3852).longitude(10.3736).address("Sdr. Boulevard 306").city("Odense").country("Denmark").user(user3).build();

            em.persist(place1);
            em.persist(place2);
            em.persist(place3);

            Story story1 = Story.builder().title("First Story").subtitle("Beginning").content("Content of the first story...").startDate(LocalDate.of(2026, 1, 1)).endDate(LocalDate.of(2026, 1, 3)).user(user1).build();
            Story story2 = Story.builder().title("Second Story").subtitle("Middle").content("Content of the second story...").startDate(LocalDate.of(2026, 1, 4)).endDate(LocalDate.of(2026, 1, 6)).user(user2).build();
            Story story3 = Story.builder().title("Third Story").subtitle("End").content("Content of the third story...").startDate(LocalDate.of(2026, 1, 7)).endDate(LocalDate.of(2026, 1, 10)).user(user3).build();

            em.persist(story1);
            em.persist(story2);
            em.persist(story3);

            TimeCapsule timeCapsule1 = TimeCapsule.builder().title("first capsule").subtitle("my first capsule").content("Memory from 2024").unlockDate(LocalDate.of(2029, 1, 1)).lockStatus(false).user(user1).build();
            TimeCapsule timeCapsule2 = TimeCapsule.builder().title("second capsule").subtitle("my second capsule").content("Open in 2030").unlockDate(LocalDate.of(2023, 1, 1)).lockStatus(true).user(user2).build();
            TimeCapsule timeCapsule3 = TimeCapsule.builder().title("third capsule").subtitle("my third capsule").content("Open in 2050").unlockDate(LocalDate.of(2023, 1, 1)).lockStatus(true).user(user3).build();

            em.persist(timeCapsule1);
            em.persist(timeCapsule2);
            em.persist(timeCapsule3);

            Mention mention1 = Mention.builder().ownerType(TargetType.MEMORY).ownerId(memory1.getId()).targetType(TargetType.PERSON).targetId(person1.getId()).startIndex(0).endIndex(6).selectedText("wedding").user(user1).build();
            Mention mention2 = Mention.builder().ownerType(TargetType.STORY).ownerId(story1.getId()).targetType(TargetType.PLACE).targetId(place1.getId()).startIndex(10).endIndex(19).selectedText("Copenhagen").user(user1).build();
            Mention mention3 = Mention.builder().ownerType(TargetType.CHAPTER).ownerId(chapter2.getId()).targetType(TargetType.PERSON).targetId(person2.getId()).startIndex(5).endIndex(10).selectedText("trine").user(user2).build();

            em.persist(mention1);
            em.persist(mention2);
            em.persist(mention3);

            Devlog devlog1 = Devlog.builder().title("notification feature").subtitle("Users are now able to get notifications").content("Update your app in order for it to work").user(user1).build();
            Devlog devlog2 = Devlog.builder().title("system update ").subtitle("Yearly update ").content("We apologize for the inconvenience").user(user1).build();
            Devlog devlog3 = Devlog.builder().title("1 year anniversary").subtitle("Congratulations").content("Thank you all").user(user2).build();

            em.persist(devlog1);
            em.persist(devlog2);
            em.persist(devlog3);

            Notification notification1 = Notification.builder().title("new friend request").notificationType(NotificationType.FRIEND_REQUEST).user(user1).build();
            Notification notification2 = Notification.builder().title("system patch").notificationType(NotificationType.DEVLOG).user(user1).build();
            Notification notification3 = Notification.builder().title("someone mentioned you in their story").notificationType(NotificationType.MENTION).user(user2).build();

            em.persist(notification1);
            em.persist(notification2);
            em.persist(notification3);

            GeminiPrompt geminiPrompt1 = GeminiPrompt.builder().content("1. Interstellar, 2. Batman, 3. The odyssey").user(user1).build();
            GeminiPrompt geminiPrompt2 = GeminiPrompt.builder().content("Write about something from your childhood").user(user1).build();
            GeminiPrompt geminiPrompt3 = GeminiPrompt.builder().content("Write about a special holiday that means a lot to you").user(user2).build();

            em.persist(geminiPrompt1);
            em.persist(geminiPrompt2);
            em.persist(geminiPrompt3);


            em.getTransaction().commit();

            return new SeededData(user1, user2, user3,
                    chapter1, chapter2, chapter3,
                    artifact1, artifact2, artifact3, artifact4,
                    musicType, objectType, vehicleType,
                    fragment1, fragment2, fragment3,
                    entityList1,
                    memory1, memory2, memory3,
                    person1, person2, person3,
                    place1, place2, place3, story1, story2, story3,
                    timeCapsule1, timeCapsule2, timeCapsule3,
                    mention1, mention2, mention3,
                    devlog1, devlog2, devlog3, notification1, notification2, notification3,
                    geminiPrompt1,geminiPrompt2,geminiPrompt3);
        }
    }
}