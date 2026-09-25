package app.controllers;

import app.dtos.User.UserResponseDTO;
import app.dtos.User.UserLoginDTO;
import app.dtos.User.UserRegistrationDTO;
import app.entities.User;
import app.mappers.AppUserMapper;
import app.services.UserService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import static io.javalin.apibuilder.ApiBuilder.*;


import java.util.Map;

public class AuthController implements EndpointGroup {

    private final UserService userService;


    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {

        try {
            UserRegistrationDTO dto = ctx.bodyAsClass(UserRegistrationDTO.class);
            User registeredUser = userService.registerUser(dto);

            UserResponseDTO responseDto = AppUserMapper.toResponseDto(registeredUser);

            ctx.status(201).json(responseDto);

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            UserLoginDTO dto = ctx.bodyAsClass(UserLoginDTO.class);
            User loggedInUser = userService.login(dto);

            UserResponseDTO responseDto = AppUserMapper.toResponseDto(loggedInUser);

            ctx.status(200).json(responseDto);

        } catch (IllegalArgumentException e) {
            ctx.status(401).json(Map.of("error", e.getMessage()));
        }
    }

    @Override
    public void addEndpoints() {
        post("/api/auth/register", this::register);
        post("/api/auth/login", this::login);
    }
}
