package app.mappers;

import app.dtos.user.RegisterRequestDTO;
import app.dtos.user.UserResponseDTO;
import app.entities.User;
import app.enums.Role;

public class UserMapper {

    public static User toEntity(RegisterRequestDTO dto, String hashedPassword) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(hashedPassword)
                .role(Role.USER)
                .build();
    }

    public static UserResponseDTO toResponse(User user) {
        if (user == null) return null;

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString()
        );
    }
}