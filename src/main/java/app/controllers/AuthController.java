package app.controllers;

import app.dtos.user.LoginResponseDTO;
import app.dtos.user.UserResponseDTO;
import app.dtos.user.LoginRequestDTO;
import app.dtos.user.RegisterRequestDTO;
import app.entities.User;
import app.mappers.UserMapper;
import app.services.UserService;
import app.utils.security.JWTToken;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import static io.javalin.apibuilder.ApiBuilder.*;


import java.util.Map;

public class AuthController implements EndpointGroup {

    // ===== Dependencies =====

    private final UserService userService;

    // ===== Constructor =====

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ===== Entity Operations =====

    public void register(Context ctx) {

        try {
            RegisterRequestDTO dto = ctx.bodyAsClass(RegisterRequestDTO.class);
            User registeredUser = userService.registerUser(dto);

            UserResponseDTO responseDto = UserMapper.toResponse(registeredUser);

            ctx.status(HttpStatus.CREATED).json(responseDto);

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequestDTO dto = ctx.bodyAsClass(LoginRequestDTO.class);
            User loggedInUser = userService.login(dto);

            String token = JWTToken.generateToken(loggedInUser.getId());
            LoginResponseDTO responseDto = new LoginResponseDTO(token);
            ctx.status(HttpStatus.OK).json(responseDto);

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.UNAUTHORIZED).json(Map.of("error", e.getMessage()));
        }
    }

    public void healthCheck(@NotNull Context ctx) {
        ctx.status(HttpStatus.OK).json("{\"msg\": \"API is up and running\"}");
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("auth/register", this::register);
        post("auth/login", this::login);
        get("auth/healthcheck", this::healthCheck);
    }
}
