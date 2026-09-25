package app.dtos.AppUser;

public record APPUserResponseDTO(
        Integer userId,
        String name,
        String email,
        String role
) {}