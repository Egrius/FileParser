package by.egrius.app.annotation;

import by.egrius.app.dto.userDTO.UserUpdateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.stream.Stream;

public class AtLeastOneFieldNotBlankValidator implements ConstraintValidator<AtLeastOneFieldNotBlank, UserUpdateDto> {
    @Override
    public boolean isValid(UserUpdateDto dto, ConstraintValidatorContext constraintValidatorContext) {
        if (dto == null) return false;

        return Stream.of(dto.getUsername(), dto.getEmail(), dto.getRawPassword())
                .anyMatch(value -> value != null && !value.isBlank());
    }
}
