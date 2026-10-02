package app.dtos.user;

public record UserResponseDTO(
        Integer userId,
        String name,
        String email,
        String role
) {}