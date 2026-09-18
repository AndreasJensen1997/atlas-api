package app.dtos;

public record UserRegistrationDTO(
        String name,
        String email,
        String password,
        String passwordCheck
) {}