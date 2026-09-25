package app.mappers;

import app.dtos.AppUser.AppUserRegistrationDTO;
import app.dtos.AppUser.APPUserResponseDTO;
import app.entities.AppUser;
import app.enums.Role;

public class AppUserMapper {


    // 1. Maps incoming Registration DTO -> New Entity
    public static AppUser registrationDTOToEntity(AppUserRegistrationDTO dto, String hashedPassword) {
        return AppUser.builder()
                .name(dto.name())
                .email(dto.email())
                .password(hashedPassword)
                .role(Role.USER)
                .build();
    }

    // 2. Maps database Entity -> Safe Response DTO (outgoing)
    public static APPUserResponseDTO toResponseDto(AppUser user) {
        if (user == null) return null;

        return new APPUserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString()
        );
    }
}