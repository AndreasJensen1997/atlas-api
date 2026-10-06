package app.dtos.user;

public record UserResponseDTO(
        Integer id,
        String name,
        String email,
        String role
) {}