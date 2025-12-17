package by.egrius.app.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UserCreateDto {
        @NotBlank(message = "Имя пользователя не должно быть пустым")
        @Size(min = 3, max = 50, message = "Имя должно быть от 3 до 50 символов")
        String username;

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Email должен быть корректным")
        String email;

        @NotBlank
        @Size(min = 4, max = 100,  message = "Пароль должен быть минимум 4 символа")
        String rawPassword;
}
