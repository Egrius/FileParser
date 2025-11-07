package by.egrius.app.annotation;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldNotBlankValidator.class)
public @interface AtLeastOneFieldNotBlank {
    String message() default "Хотя бы одно поле должно быть заполнено";
    Class<?>[] groups() default {}; // ← обязательно
    Class<? extends jakarta.validation.Payload>[] payload() default {};
}
