package by.egrius.app.dto.userDTO;

import lombok.Value;

@Value
public class UserUpdateDto {
    String username;
    String email;
    String rawPassword;
}
