package app.dtos.User;

public record UserRegistrationDTO(
        String name,
        String email,
        String password,
        String passwordCheck
) {}