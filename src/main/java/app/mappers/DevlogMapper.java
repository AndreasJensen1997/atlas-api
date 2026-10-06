package app.mappers;

import app.dtos.devlog.DevlogRequestDTO;
import app.dtos.devlog.DevlogResponseDTO;
import app.entities.Devlog;
import app.entities.User;
import app.mappers.generics.IMapper;

public class DevlogMapper implements IMapper<DevlogRequestDTO, DevlogResponseDTO, Devlog, User> {

    @Override
    public Devlog toEntity(DevlogRequestDTO dto, User user) {
        if (dto == null) return null;
        return Devlog.builder()
                .title(dto.title())
                .subtitle(dto.subtitle())
                .content(dto.content())
                .user(user)
                .build();

    }

    @Override
    public DevlogResponseDTO toResponse(Devlog devlog) {
        if (devlog == null) return null;

        return new DevlogResponseDTO(
                devlog.getId(),
                devlog.getTitle(),
                devlog.getSubtitle(),
                devlog.getContent(),
                devlog.getCreatedAt()

        );
    }
}
