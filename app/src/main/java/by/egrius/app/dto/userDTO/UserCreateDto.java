package by.egrius.app.dto.userDTO;

import lombok.Value;

@Value
public class UserCreateDto {
        String username;
        String email;
        String rawPassword;
}
