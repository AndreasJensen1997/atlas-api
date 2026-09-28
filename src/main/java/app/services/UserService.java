package app.services;

import app.daos.user.UserDAO;
import app.dtos.User.LoginRequestDTO;
import app.dtos.User.RegisterRequestDTO;
import app.entities.User;
import app.mappers.UserMapper;
import app.utils.Validation.UserValidator;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {

    private final UserDAO userDao;

    public UserService(UserDAO userDao) {
        this.userDao = userDao;
    }

    public User registerUser(RegisterRequestDTO dto) {
        List<String> validationErrors = UserValidator.validateRegistration(dto);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", validationErrors));
        }

        if (userDao.getUserByEmail(dto.email()) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        User newUser = UserMapper.registrationDTOToEntity(dto, hashedPassword);

        return userDao.create(newUser);
    }


    public User login(LoginRequestDTO loginRequestDTO) {

        if (loginRequestDTO.email() == null || loginRequestDTO.password() == null) {
            throw new IllegalArgumentException("Email and password must be provided.");
        }

        User user = userDao.getUserByEmail(loginRequestDTO.email());

        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        boolean isPasswordCorrect = BCrypt.checkpw(loginRequestDTO.password(), user.getPassword());

        if (!isPasswordCorrect) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        System.out.println("login success");

        return user;
    }

    public User getByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must be provided.");
        }

        User user = userDao.getUserByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("No user was found attached to this email");
        }

        System.out.println("user successfully found :" + user.getName() + ": " + user.getEmail());

        return user;
    }

    public User getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Email must be provided.");
        }

        User user = userDao.getById(id);

        if (user == null) {
            throw new IllegalArgumentException("No user was found attached to this email");
        }

        System.out.println("user successfully found :" + user.getName() + ": " + user.getUserId());

        return user;
    }
}