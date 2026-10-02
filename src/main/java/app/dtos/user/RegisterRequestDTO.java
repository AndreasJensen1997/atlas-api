package app.dtos.user;

public record RegisterRequestDTO(
        String name,
        String email,
        String password,
        String passwordCheck
) {}