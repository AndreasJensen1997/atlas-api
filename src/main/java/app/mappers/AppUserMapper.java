package app.mappers;

import app.dtos.UserRegistrationDTO;
import app.dtos.UserResponseDTO;
import app.entities.AppUser;
import app.enums.Role;

public class AppUserMapper {
    public static AppUser registrationDTOToEntity(UserRegistrationDTO dto, String hashedPassword) {
        return AppUser.builder()
                .name(dto.name())
                .email(dto.email())
                .password(hashedPassword)
                .role(Role.USER)
                .build();
    }

    public static UserResponseDTO toResponseDto(AppUser user) {
        if (user == null) return null;

        return new UserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString()
        );
    }
}