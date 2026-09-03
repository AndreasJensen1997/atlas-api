package app;

import app.config.HibernateConfig;
import app.daos.AppUserDAO;

import app.daos.ChapterDAO;
import app.entities.AppUser;
import app.entities.Chapter;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        AppUserDAO appUserDAO = new AppUserDAO(emf);
        ChapterDAO chapterDAO = new ChapterDAO(emf);

        AppUser andreas = AppUser.builder().name("andreas").email("andreas.jensen@outlook.dk").password("1234").build();
        Chapter chapter = Chapter.builder().title("years in china").subtitle("my time in china in school").startDate(LocalDate.of(2012,1,1)).endDate(LocalDate.of(2014,1,1)).build();

        appUserDAO.create(andreas);
        chapterDAO.create(chapter);



        emf.close();


    }


}
