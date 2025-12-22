package by.egrius.app.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordOrEmptyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPasswordOrEmpty {
    String message() default "Пароль должен быть минимум 4 символа или пустым";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
