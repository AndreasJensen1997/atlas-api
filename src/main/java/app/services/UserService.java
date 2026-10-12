package app.services;

import app.daos.user.UserDAO;
import app.dtos.user.LoginRequestDTO;
import app.dtos.user.RegisterRequestDTO;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.UserMapper;
import app.utils.validation.UserValidator;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {

    // ===== Dependencies =====

    private final UserDAO userDao;

    // ===== Constructor =====

    public UserService(UserDAO userDao) {
        this.userDao = userDao;
    }

    // ===== Register =====

    public User registerUser(RegisterRequestDTO dto) {
        List<String> validationErrors = UserValidator.validateRegistration(dto);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", validationErrors));
        }

        String email = User.normalize(dto.email());

        if (userDao.getUserByEmail(email) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        User newUser = UserMapper.toEntity(dto, hashedPassword);

        return userDao.create(newUser);
    }

    // ===== Login =====

    public User login(LoginRequestDTO loginRequestDTO) {

        if (loginRequestDTO.email() == null || loginRequestDTO.password() == null) {
            throw new IllegalArgumentException("Email and password must be provided.");
        }

        User user = userDao.getUserByEmail(User.normalize(loginRequestDTO.email()));

        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        boolean isPasswordCorrect = BCrypt.checkpw(loginRequestDTO.password(), user.getPassword());

        if (!isPasswordCorrect) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }

    // ===== Read =====

    public User getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("User id  must be provided.");
        }

        User user = userDao.getById(id);

        if (user == null) {
            throw new IllegalArgumentException("No user was found attached to this email");
        }

        return user;
    }

    public List<User> getAll() {
        return userDao.getAll();
    }

    // ===== Update =====

    public User update(Integer userId, String newName, String newEmail) {
        User user = getById(userId);

        if (newEmail != null && !newEmail.isBlank()) {
            String normalized = User.normalize(newEmail);
            User existing = userDao.getUserByEmail(normalized);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new ApiException(409, "An account with this email already exists.");
            }
            user.setEmail(normalized);
            user.setName(newName);
        }

        return userDao.update(user);
    }

    // ===== Delete =====

    public void delete(Integer id) {
        User user = getById(id);
        userDao.delete(user.getId());
    }
}