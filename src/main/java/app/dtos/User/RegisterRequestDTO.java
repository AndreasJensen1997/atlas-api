package app.dtos.User;

public record RegisterRequestDTO(
        String name,
        String email,
        String password,
        String passwordCheck
) {}