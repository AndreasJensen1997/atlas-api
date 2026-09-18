package app.controllers;

import app.dtos.UserLoginDTO;
import app.dtos.UserRegistrationDTO;
import app.entities.AppUser;
import app.services.UserService;
import io.javalin.http.Context;
import java.util.Map;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            UserRegistrationDTO dto = ctx.bodyAsClass(UserRegistrationDTO.class);
            AppUser registeredUser = userService.registerUser(dto);

            ctx.status(201).json(Map.of(
                    "message", "User registered successfully!",
                    "email", registeredUser.getEmail(),
                    "name", registeredUser.getName()
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            UserLoginDTO dto = ctx.bodyAsClass(UserLoginDTO.class);
            AppUser loggedInUser = userService.login(dto);

            ctx.status(200).json(Map.of(
                    "message", "Login successful!",
                    "email", loggedInUser.getEmail(),
                    "name", loggedInUser.getName(),
                    "role", loggedInUser.getRole().toString()
            ));
        } catch (IllegalArgumentException e) {
            // 401 Unauthorized for bad credentials
            ctx.status(401).json(Map.of("error", e.getMessage()));
        }
    }
}