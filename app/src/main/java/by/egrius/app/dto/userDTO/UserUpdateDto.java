package by.egrius.app.dto.userDTO;

import by.egrius.app.annotation.AtLeastOneFieldNotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Value;

@Value
@AtLeastOneFieldNotBlank
public class UserUpdateDto {
    String username;

    @Email
    String email;

    String rawPassword;
}
