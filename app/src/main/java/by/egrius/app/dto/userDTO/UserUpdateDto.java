package by.egrius.app.dto.userDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Value;

@Value
public class UserUpdateDto {
    String username;
    String email;
    String rawPassword;
}
