package app.controllers;

import app.dtos.user.UserResponseDTO;
import app.dtos.user.UserUpdateDTO;
import app.entities.User;
import app.mappers.UserMapper;
import app.services.UserService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.*;

import java.util.List;

public class UserController implements EndpointGroup {

    // ===== Dependencies =====

    private final UserService userService;

    // ===== Constructor =====

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ===== Read =====

    public void getCurrentUser(Context ctx) {
        Integer currentUserId = ctx.attribute("userId");

        if (currentUserId == null) {
            throw new IllegalArgumentException("User is not authenticated.");
        }
        User user = userService.getById(currentUserId);
        ctx.json(UserMapper.toResponse(user));
    }

    public void getById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));

        if (id <= 0) {
            throw new IllegalArgumentException("ID must be a positive integer.");
        }
        User user = userService.getById(id);
        ctx.json(UserMapper.toResponse(user));
    }

    public void getAll(Context ctx) {
        List<User> allUsers = userService.getAll();

        List<UserResponseDTO> responseDTOList = allUsers.stream()
                .map(UserMapper::toResponse)
                .toList();
        ctx.json(responseDTOList);
    }

    // ===== Update =====

    public void update(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Integer currentUserId = ctx.attribute("userId");

        if (currentUserId == null) {
            throw new IllegalArgumentException("User is not authenticated.");
        }
        if (id != currentUserId) {
            throw new IllegalArgumentException("You can only update your own account.");
        }
        UserUpdateDTO updateDTO = ctx.bodyAsClass(UserUpdateDTO.class);
        User updatedUser = userService.update(id, updateDTO.name(), updateDTO.email());

        ctx.json(UserMapper.toResponse(updatedUser));
    }

    // ===== Delete =====

    public void deleteUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Integer currentUserId = ctx.attribute("userId");

        if (currentUserId == null) {
            throw new IllegalArgumentException("User is not authenticated.");
        }
        if (id != currentUserId) {
            throw new IllegalArgumentException("You can only delete your own account.");
        }

        userService.delete(id);
        ctx.status(204);
    }

    @Override
    public void addEndpoints() {
        get("api/users/current", this::getCurrentUser);
        get("api/users/{id}", this::getById);
        get("api/users", this::getAll);
        put("api/users/{id}", this::update);
        delete("api/users/{id}", this::deleteUser);
    }
}
