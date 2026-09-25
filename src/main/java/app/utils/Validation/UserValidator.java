package app.utils.Validation;

import app.dtos.User.UserRegistrationDTO;
import java.util.ArrayList;
import java.util.List;

public class UserValidator {


    public static List<String> validateRegistration(UserRegistrationDTO dto) {

        List<String> errors = new ArrayList<>();

        if (dto == null) {
            errors.add("Registration data cannot be null");
            return errors;
        }

        // 1. Name validation
        if (dto.name() == null || dto.name().isBlank()) {
            errors.add("Name needs to be filled");
        }

        // 2. Email validation
        if (dto.email() == null || dto.email().isBlank()) {
            errors.add("Email needs to be filled");
        } else {
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!dto.email().matches(emailRegex)) {
                errors.add("Email should contain @ and .");
            }
        }

        // 3. Password validation
        String password = dto.password();
        if (password == null || password.isBlank()) {
            errors.add("password needs to be filled");
        } else {
            if (password.length() < 8) {
                errors.add("Password is to short(min. 8 characters)");
            }
            if (!password.chars().anyMatch(Character::isDigit)) {
                errors.add("Password must contain at least one number");
            }
            if (!password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) {
                errors.add("Password must contain at least one special character");
            }
        }

        // 4. Password match validation
        if (dto.passwordCheck() == null || !dto.passwordCheck().equals(password)) {
            errors.add("Passwords must match");
        }

        return errors;
    }
}