package by.egrius.app.dto.userDTO;

import java.time.LocalDate;
import java.util.UUID;

public record UserReadDto(
        UUID userId,
        String username,
        String email,
        LocalDate createdAt
) {}
