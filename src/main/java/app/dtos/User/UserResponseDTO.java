package app.dtos.User;

public record UserResponseDTO(
        Integer userId,
        String name,
        String email,
        String role
) {}