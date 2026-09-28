package app.controllers;

import app.dtos.User.LoginResponseDTO;
import app.dtos.User.UserResponseDTO;
import app.dtos.User.LoginRequestDTO;
import app.dtos.User.RegisterRequestDTO;
import app.entities.User;
import app.mappers.AppUserMapper;
import app.services.UserService;
import app.utils.security.JWTToken;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
//
import static io.javalin.apibuilder.ApiBuilder.*;


import java.util.Map;

public class AuthController implements EndpointGroup {

    private final UserService userService;


    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {

        try {
            RegisterRequestDTO dto = ctx.bodyAsClass(RegisterRequestDTO.class);
            User registeredUser = userService.registerUser(dto);

            UserResponseDTO responseDto = AppUserMapper.toResponseDto(registeredUser);

            ctx.status(201).json(responseDto);

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequestDTO dto = ctx.bodyAsClass(LoginRequestDTO.class);
            User loggedInUser = userService.login(dto);

            String token = JWTToken.generateToken(loggedInUser.getEmail());
            LoginResponseDTO responseDto = new LoginResponseDTO(token);
            ctx.status(HttpStatus.OK).json(responseDto);

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
