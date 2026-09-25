package app.controllers;

import app.dtos.AppUser.AppUserLoginDTO;
import app.dtos.AppUser.AppUserRegistrationDTO;
import app.dtos.AppUser.APPUserResponseDTO;
import app.entities.AppUser;
import app.mappers.AppUserMapper;
import app.services.AppUserService;
import io.javalin.http.Context;
import java.util.Map;

public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public void register(Context ctx) {
        try {
            AppUserRegistrationDTO dto = ctx.bodyAsClass(AppUserRegistrationDTO.class);
            AppUser registeredUser = appUserService.registerUser(dto);

            APPUserResponseDTO responseDto = AppUserMapper.toResponseDto(registeredUser);

            ctx.status(201).json(responseDto);

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            AppUserLoginDTO dto = ctx.bodyAsClass(AppUserLoginDTO.class);
            AppUser loggedInUser = appUserService.login(dto);

            APPUserResponseDTO responseDto = AppUserMapper.toResponseDto(loggedInUser);

            ctx.status(200).json(responseDto);

        } catch (IllegalArgumentException e) {
            ctx.status(401).json(Map.of("error", e.getMessage()));
        }
    }
}