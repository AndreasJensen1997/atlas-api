package app.services;

import app.daos.userOwned.MemoryDAO;
import app.dtos.memory.MemoryRequestDTO;
import app.entities.Memory;
import app.entities.User;
import app.mappers.MemoryMapper;

import java.util.List;

public class MemoryService {

    private final MemoryDAO memoryDAO;
    private final UserService userService;
    private final MemoryMapper memoryMapper;

    public MemoryService(MemoryDAO memoryDAO, UserService userService, MemoryMapper memoryMapper) {
        this.memoryDAO = memoryDAO;
        this.userService = userService;
        this.memoryMapper = memoryMapper;
    }

    public Memory createMemory(MemoryRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        if (memoryDAO.findByTitleAndUserId(dto.title(), owner.getUserId()) != null) {
            throw new IllegalArgumentException("A memory with this title already exists.");
        }

        Memory newMemory = memoryMapper.toEntity(dto, owner);

        return memoryDAO.create(newMemory);
    }

    public Memory getById(Integer memoryId, int userId) {
        Memory memory = memoryDAO.getById(memoryId);

        if (memory == null) {
            throw new IllegalArgumentException("Memory not found with ID: " + memoryId);
        }

        if (!memory.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this memory.");
        }
        return memory;
    }

    public List<Memory> getAllById(int userId) {
        return memoryDAO.getAllByUserId(userId);
    }

    public void delete(Integer memoryId, int userId) {
        Memory memory = getById(memoryId, userId);
        memoryDAO.delete(memory.getId());
    }

    public Memory update(Integer memoryId, MemoryRequestDTO dto, int userId) {
        Memory memory = getById(memoryId, userId);

        memory.setTitle(dto.title());
        memory.setSubtitle(dto.subtitle());
        memory.setContent(dto.content());
        memory.setDate(dto.date());
        memory.setVisibility(dto.visibility());

        return memoryDAO.update(memory);
    }
}