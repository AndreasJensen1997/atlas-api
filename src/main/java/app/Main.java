package app;

import app.config.HibernateConfig;
import app.daos.AppUserDAO;

import app.daos.userOwned.ChapterDAO;
import app.daos.userOwned.MentionDAO;
import app.dtos.GeminiResponseDTO;
import app.dtos.UserLoginDTO;
import app.dtos.UserRegistrationDTO;
import app.entities.AppUser;
import app.entities.Chapter;
import app.entities.Mention;
import app.enums.TargetType;
import app.service.GeminiAPIReader;
import app.service.UserService;
import jakarta.persistence.EntityManagerFactory;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        AppUserDAO appUserDAO = new AppUserDAO(emf);
        UserService userService = new UserService(appUserDAO);

        UserRegistrationDTO user1 = new UserRegistrationDTO("Andreas", "andreas.jensen@outlook.dk", "12345678!", "12345678!");
        UserRegistrationDTO user2 = new UserRegistrationDTO("Morten", "morten.h@outlook.dk", "12345678!", "12345678!");
        UserLoginDTO userLoginDTO1 = new UserLoginDTO("morten.h@outlook.dk", "12345678!");

        userService.registerUser(user1);
        userService.registerUser(user2);


        userService.login(userLoginDTO1);


//
//        GeminiAPIReader reader = new GeminiAPIReader();
//        ObjectMapper mapper = new ObjectMapper();
//
//        System.out.println(reader.askGemini("give me an idea of something i can write about in my journal"));


        emf.close();


    }


}
