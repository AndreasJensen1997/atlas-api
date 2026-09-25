package app.controllers;

import app.dtos.AppUser.AppUserLoginDTO;
import app.dtos.AppUser.AppUserRegistrationDTO;
import app.dtos.AppUser.APPUserResponseDTO;
import app.entities.AppUser;
import app.mappers.AppUserMapper;
import app.services.AppUserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;

public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

}