package app.services;

import app.daos.userOwned.DevlogDAO;
import app.dtos.devlog.DevlogRequestDTO;
import app.entities.Devlog;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.DevlogMapper;

import java.util.List;

public class DevlogService {

    // ===== Dependencies =====

    private final DevlogDAO devlogDAO;
    private final UserService userService;
    private final DevlogMapper devlogMapper;

    // ===== Constructor =====

    public DevlogService(DevlogDAO devlogDAO, UserService userService, DevlogMapper devlogMapper) {
        this.devlogDAO = devlogDAO;
        this.userService = userService;
        this.devlogMapper = devlogMapper;
    }

    // ===== Create =====

    public Devlog createDevlog(DevlogRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        if (devlogDAO.findByTitleAndUserId(dto.title(), owner.getUserId()) != null) {
            throw new IllegalArgumentException("A devlog with this title already exists.");
        }

        Devlog newDevlog = devlogMapper.toEntity(dto, owner);

        return devlogDAO.create(newDevlog);
    }

    // ===== Read =====

    public Devlog getById(Integer devlogId, int userId) {
        Devlog devlog = devlogDAO.getById(devlogId);

        if (devlog == null) {
            throw new IllegalArgumentException("Chapter not found with ID: " + devlogId);
        }

        if (!devlog.getUser().getUserId().equals(userId)) {
            throw new ApiException(404, "Chapter not found with ID: " + devlogId);
        }
        return devlog;
    }

    public List<Devlog> getAllById(int userId) {

        return devlogDAO.getAllByUserId(userId);
    }

    // ===== Update =====

    public Devlog update(Integer devlogId, DevlogRequestDTO dto, int userId) {
        Devlog devlog = devlogDAO.getById(devlogId);

        if (devlog == null) {
            throw new IllegalArgumentException("Chapter not found with ID: " + devlogId);
        }

        if (!devlog.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this chapter.");
        }

        devlog.setTitle(dto.title());
        devlog.setSubtitle(dto.subtitle());
        devlog.setContent(dto.content());

        return devlogDAO.update(devlog);
    }

    // ===== Delete =====

    public void delete(Integer devlogId, int userId) {
        Devlog devlog = getById(devlogId, userId);

        devlogDAO.delete(devlog.getDevlogId());
    }
}
