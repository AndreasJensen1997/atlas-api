package app.services;

import app.daos.userOwned.TimeCapsuleDAO;
import app.dtos.timeCapsule.TimeCapsuleRequestDTO;
import app.entities.TimeCapsule;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.TimeCapsuleMapper;

import java.util.List;

public class TimeCapsuleService {

    // ===== Dependencies =====

    private final TimeCapsuleDAO timeCapsuleDAO;
    private final UserService userService;
    private final TimeCapsuleMapper timeCapsuleMapper;

    // ===== Constructor =====

    public TimeCapsuleService(TimeCapsuleDAO timeCapsuleDAO, UserService userService, TimeCapsuleMapper timeCapsuleMapper) {
        this.timeCapsuleDAO = timeCapsuleDAO;
        this.userService = userService;
        this.timeCapsuleMapper = timeCapsuleMapper;
    }

    // ===== Create =====

    public TimeCapsule createTimeCapsule(TimeCapsuleRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        if (timeCapsuleDAO.findByTitleAndUserId(dto.title(), owner.getUserId()) != null) {
            throw new IllegalArgumentException("A time capsule with this title already exists.");
        }

        TimeCapsule newTimeCapsule = timeCapsuleMapper.toEntity(dto, owner);
        return timeCapsuleDAO.create(newTimeCapsule);
    }

    // ===== Read =====

    public TimeCapsule getById(Integer timeCapsuleId, int userId) {
        TimeCapsule timeCapsule = timeCapsuleDAO.getById(timeCapsuleId);

        if (timeCapsule == null) {
            throw new IllegalArgumentException("Time capsule not found with ID: " + timeCapsuleId);
        }

        if (!timeCapsule.getUser().getUserId().equals(userId)) {
            throw new ApiException(404, "Chapter not found with ID: " + timeCapsuleId);
        }
        return timeCapsule;
    }

    public List<TimeCapsule> getAllById(int userId) {
        return timeCapsuleDAO.getAllByUserId(userId);
    }

    // ===== Delete =====

    public void delete(Integer timeCapsuleId, int userId) {
        TimeCapsule timeCapsule = getById(timeCapsuleId, userId);
        timeCapsuleDAO.delete(timeCapsule.getTimeCapsuleId());
    }
}