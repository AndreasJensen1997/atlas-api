package app;

import app.config.HibernateConfig;
import app.daos.AppUserDAO;

import app.daos.ChapterDAO;
import app.daos.MentionDAO;
import app.entities.AppUser;
import app.entities.Chapter;
import app.entities.Mention;
import app.enums.TargetType;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        AppUserDAO appUserDAO = new AppUserDAO(emf);
        ChapterDAO chapterDAO = new ChapterDAO(emf);
        MentionDAO mentionDAO = new MentionDAO(emf);

        AppUser andreas = AppUser.builder().name("andreas").email("andreas.jensen@outlook.dk").password("1234").build();
        Chapter chapter1 = Chapter.builder().title("years in china").subtitle("my time in china in school").startDate(LocalDate.of(2012,1,1)).endDate(LocalDate.of(2014,1,1)).appUser(andreas).build();
        Chapter chapter2 = Chapter.builder().title("years in japan").subtitle("my solo trip to japan").startDate(LocalDate.of(2020,1,1)).endDate(LocalDate.of(2014,1,1)).appUser(andreas).build();

        appUserDAO.create(andreas);
        chapterDAO.create(chapter1);
        chapterDAO.create(chapter2);

        Mention mention = Mention.builder()
                .ownerId(chapter1.getChapterId())
                .ownerType(TargetType.CHAPTER)
                .startIndex(2)
                .endIndex(10)
                .selectedText("Text")
                .targetId(chapter2.getChapterId())
                .targetType(TargetType.CHAPTER)
                .build();

        mentionDAO.create(mention);

        System.out.println(mention);


        emf.close();


    }


}
