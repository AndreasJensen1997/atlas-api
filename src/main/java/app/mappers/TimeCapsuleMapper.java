package app.mappers;

import app.dtos.timeCapsule.TimeCapsuleRequestDTO;
import app.dtos.timeCapsule.TimeCapsuleResponseDTO;
import app.entities.TimeCapsule;
import app.entities.User;
import app.mappers.generics.IMapper;

public class TimeCapsuleMapper implements IMapper<TimeCapsuleRequestDTO, TimeCapsuleResponseDTO, TimeCapsule, User> {

    @Override
    public TimeCapsule toEntity(TimeCapsuleRequestDTO dto, User user) {
        if (dto == null) return null;
        return TimeCapsule.builder()
                .title(dto.title())
                .subtitle(dto.subtitle())
                .content(dto.content())
                .unlockDate(dto.unlockDate())
                .user(user)
                .build();
    }

    @Override
    public TimeCapsuleResponseDTO toResponse(TimeCapsule timeCapsule) {
        if (timeCapsule == null) return null;

        return new TimeCapsuleResponseDTO(
                timeCapsule.getId(),
                timeCapsule.getTitle(),
                timeCapsule.getSubtitle(),
                timeCapsule.getContent(),
                timeCapsule.getUnlockDate(),
                timeCapsule.isLockStatus()

        );
    }
}
