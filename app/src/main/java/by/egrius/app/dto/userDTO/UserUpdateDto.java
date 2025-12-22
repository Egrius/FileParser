package by.egrius.app.dto.userDTO;

import by.egrius.app.annotation.AtLeastOneFieldNotBlank;
import by.egrius.app.annotation.ValidPasswordOrEmpty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@AtLeastOneFieldNotBlank
public class UserUpdateDto {
    @Size(min = 3, max = 50, message = "Имя должно быть от 3 до 50 символов")
    String username;

    @Email(message = "Email должен быть корректным")
    String email;


    @ValidPasswordOrEmpty
    String rawPassword;
}
