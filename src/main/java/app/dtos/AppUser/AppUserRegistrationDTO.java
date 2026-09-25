package app.dtos.AppUser;

public record AppUserRegistrationDTO(
        String name,
        String email,
        String password,
        String passwordCheck
) {}