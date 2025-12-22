package by.egrius.app.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordOrEmptyValidator implements ConstraintValidator<ValidPasswordOrEmpty, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if (password == null || password.trim().isEmpty()) {
            return true;
        }

        return password.length() >= 4 && password.length() <= 100;
    }
}