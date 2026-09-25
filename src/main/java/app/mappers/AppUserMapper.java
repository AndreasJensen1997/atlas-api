package app.mappers;

import app.dtos.User.UserRegistrationDTO;
import app.dtos.User.UserResponseDTO;
import app.entities.User;
import app.enums.Role;

public class AppUserMapper {


    // 1. Maps incoming Registration DTO -> New Entity
    public static User registrationDTOToEntity(UserRegistrationDTO dto, String hashedPassword) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(hashedPassword)
                .role(Role.USER)
                .build();
    }

    // 2. Maps database Entity -> Safe Response DTO (outgoing)
    public static UserResponseDTO toResponseDto(User user) {
        if (user == null) return null;

        return new UserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString()
        );
    }
}