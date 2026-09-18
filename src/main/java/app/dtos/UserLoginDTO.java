package app.dtos;

public record UserLoginDTO(
        String email,
        String password
) {}