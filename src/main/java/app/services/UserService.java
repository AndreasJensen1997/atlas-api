package app.services;

import app.daos.UserDAO;
import app.dtos.User.UserLoginDTO;
import app.dtos.User.UserRegistrationDTO;
import app.entities.User;
import app.mappers.AppUserMapper;
import app.utils.Validation.UserValidator;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {

    private final UserDAO userDao;

    public UserService(UserDAO userDao) {
        this.userDao = userDao;
    }

    public User registerUser(UserRegistrationDTO dto) {
        List<String> validationErrors = UserValidator.validateRegistration(dto);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", validationErrors));
        }

        if (userDao.getUserByEmail(dto.email()) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        User newUser = AppUserMapper.registrationDTOToEntity(dto,hashedPassword);

         return userDao.create(newUser);
    }


    public User login(UserLoginDTO userLoginDTO) {

        if (userLoginDTO.email() == null || userLoginDTO.password() == null) {
            throw new IllegalArgumentException("Email and password must be provided.");
        }

        User user = userDao.getUserByEmail(userLoginDTO.email());

        System.out.println("Email from login: " + userLoginDTO.email());
        System.out.println("User found: " + user);

        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        boolean isPasswordCorrect = BCrypt.checkpw(userLoginDTO.password(), user.getPassword());

        if (!isPasswordCorrect) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        System.out.println("login succesfull");

        return user;
    }
}