package app.dtos.User;

public record LoginRequestDTO(
        String email,
        String password
) {}