package app.mappers;

import app.dtos.memory.MemoryRequestDTO;
import app.dtos.memory.MemoryResponseDTO;
import app.entities.Memory;
import app.entities.User;
import app.mappers.generics.IMapper;

public class MemoryMapper implements IMapper<MemoryRequestDTO, MemoryResponseDTO, Memory, User> {

    @Override
    public Memory toEntity(MemoryRequestDTO dto, User user) {
        if (dto == null) return null;
        return Memory.builder()
                .title(dto.title())
                .subtitle(dto.subtitle())
                .content(dto.content())
                .date(dto.date())
                .visibility(dto.visibility())
                .user(user)
                .build();

    }

    @Override
    public MemoryResponseDTO toResponse(Memory memory) {
        if (memory == null) return null;

        return new MemoryResponseDTO(
                memory.getId(),
                memory.getTitle(),
                memory.getSubtitle(),
                memory.getContent(),
                memory.getDate(),
                memory.getCreatedAt(),
                memory.getUpdatedAt(),
                memory.getVisibility()

        );
    }
}