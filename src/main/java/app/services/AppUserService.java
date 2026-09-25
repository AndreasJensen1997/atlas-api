package app.services;

import app.daos.AppUserDAO;
import app.dtos.AppUser.AppUserLoginDTO;
import app.dtos.AppUser.AppUserRegistrationDTO;
import app.entities.AppUser;
import app.mappers.AppUserMapper;
import app.utils.Validation.UserValidator;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class AppUserService {

    private final AppUserDAO userDao;

    public AppUserService(AppUserDAO userDao) {
        this.userDao = userDao;
    }

    public AppUser registerUser(AppUserRegistrationDTO dto) {
        List<String> validationErrors = UserValidator.validateRegistration(dto);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", validationErrors));
        }

        if (userDao.getUserByEmail(dto.email()) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        AppUser newUser = AppUserMapper.registrationDTOToEntity(dto,hashedPassword);

         return userDao.create(newUser);
    }


    public AppUser login(AppUserLoginDTO appUserLoginDTO) {

        if (appUserLoginDTO.email() == null || appUserLoginDTO.password() == null) {
            throw new IllegalArgumentException("Email and password must be provided.");
        }

        AppUser user = userDao.getUserByEmail(appUserLoginDTO.email());

        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        boolean isPasswordCorrect = BCrypt.checkpw(appUserLoginDTO.password(), user.getPassword());

        if (!isPasswordCorrect) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        System.out.println("login succesfull");

        return user;
    }
}