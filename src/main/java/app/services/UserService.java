package app.services;

import app.daos.AppUserDAO;
import app.dtos.UserLoginDTO;
import app.dtos.UserRegistrationDTO;
import app.entities.AppUser;
import app.enums.Role;
import app.utils.UserValidator;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {

    private final AppUserDAO userDao;

    public UserService(AppUserDAO userDao) {
        this.userDao = userDao;
    }

    public AppUser registerUser(UserRegistrationDTO dto) {
        List<String> validationErrors = UserValidator.validateRegistration(dto);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", validationErrors));
        }

        if (userDao.getUserByEmail(dto.email()) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = BCrypt.hashpw(dto.password(), BCrypt.gensalt());

        AppUser newUser = AppUser.builder()
                .name(dto.name())
                .email(dto.email())
                .password(hashedPassword)
                .role(Role.USER)
                .build();

         return userDao.create(newUser);
    }


    public AppUser login(UserLoginDTO userLoginDTO) {

        if (userLoginDTO.email() == null || userLoginDTO.password() == null) {
            throw new IllegalArgumentException("Email and password must be provided.");
        }

        AppUser user = userDao.getUserByEmail(userLoginDTO.email());

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