package by.egrius.app.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UserCreateDto {
        @NotBlank
        String username;

        @NotBlank
        @Email
        String email;

        @NotBlank
        @Size(min = 4, max = 100)
        String rawPassword;
}
