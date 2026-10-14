package app.services;

import app.daos.userOwned.DevlogDAO;
import app.dtos.devlog.DevlogRequestDTO;
import app.entities.Devlog;
import app.entities.User;
import app.mappers.DevlogMapper;

import java.util.List;
import java.util.concurrent.Executor;

public class DevlogService {

    // ===== Dependencies =====

    private final DevlogDAO devlogDAO;
    private final UserService userService;
    private final DevlogMapper devlogMapper;
    private final NotificationService notificationService;
    private final Executor executor;

    // ===== Constructor =====

    public DevlogService(DevlogDAO devlogDAO, UserService userService, DevlogMapper devlogMapper, NotificationService notificationService, Executor executor) {
        this.devlogDAO = devlogDAO;
        this.userService = userService;
        this.devlogMapper = devlogMapper;
        this.notificationService = notificationService;
        this.executor = executor;
    }

    // ===== Create =====

    public Devlog createDevlog(DevlogRequestDTO dto, int userId) {
        User owner = userService.getById(userId);
        Devlog saved = devlogDAO.create(devlogMapper.toEntity(dto, owner));
        executor.execute(() -> notificationService.notifyAllUsers(saved));

        return saved;
    }

    // ===== Read =====

    public Devlog getById(Integer devlogId) {
        return devlogDAO.getById(devlogId);
    }

    public List<Devlog> getAll() {
        return devlogDAO.getAll();
    }

    // ===== Update =====

    public Devlog update(Integer devlogId, DevlogRequestDTO dto) {
        Devlog devlog = getById(devlogId);

        devlog.setTitle(dto.title());
        devlog.setSubtitle(dto.subtitle());
        devlog.setContent(dto.content());

        return devlogDAO.update(devlog);
    }

    // ===== Delete =====

    public void delete(Integer devlogId) {
        Devlog devlog = getById(devlogId);
        devlogDAO.delete(devlog.getId());
    }
}
