package by.egrius.app.unit.controller;

import by.egrius.app.controller.UserController;
import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_shouldReturn400_whenInvalidDto() throws Exception {
        String invalidJson = """
                {
                    "username": "",
                    "email": "not-an-email",
                    "rawPassword": "123"
                }
                """;

        mockMvc.perform(post("/user/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(3)
                );
    }

    @Test
    void createUser_shouldReturn200_whenValidDto() throws Exception {
        String validJson = """
                {
                    "username": "TestUser",
                    "email": "test_user@gmail.com",
                    "rawPassword": "1234"
                }
                """;

        UserReadDto mockResponse = new UserReadDto(UUID.randomUUID(), "TestUser", "test_user@gmail.com", LocalDate.now());

        when(userService.createUser(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/user/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("TestUser"))
                .andExpect(jsonPath("$.email").value("test_user@gmail.com"));
    }

    @Test
    void createUser_shouldReturn400_whenPassword_length_less_than_4() throws Exception {
        String invalidJson = """
                {
                    "username": "TestUser",
                    "email": "test_user@gmail.com",
                    "rawPassword": "123"
                }
                """;

        mockMvc.perform(post("/user/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1));
    }

    @Test
    void getUserByUsername_shouldReturn200_whenNameIsCorrect() throws Exception {
        UserReadDto mockDto = new UserReadDto(
                UUID.randomUUID(),
                "A",
                "testgmail@gmail.com",
                LocalDate.now()
        );

        String username = mockDto.username();

        when(userService.getUserByUsername(username)).thenReturn(Optional.of(mockDto));

        mockMvc.perform(get("/user/by-username/A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("A"))
                .andExpect(jsonPath("$.email").value("testgmail@gmail.com"));
    }

    @Test
    void getUserByUsername_shouldReturn404_whenNameNotFound() throws Exception {

        when(userService.getUserByUsername("A")).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/by-username/A"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUserId_shouldReturn200_whenIdFound() throws Exception {
        UserReadDto mockDto = new UserReadDto(
                UUID.randomUUID(),
                "A",
                "testgmail@gmail.com",
                LocalDate.now()
        );

        UUID mockId = mockDto.userId();

        when(userService.getUserById(mockId)).thenReturn(Optional.of(mockDto));

        mockMvc.perform(get("/user/by-id/" + mockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(mockDto.username()))
                .andExpect(jsonPath("$.userId").value(mockDto.userId().toString()))
                .andExpect(jsonPath("$.email").value(mockDto.email()));
    }

    @Test
    void getUserByUserId_shouldReturn404_whenIdNotFound() throws Exception {

        UUID mockId = UUID.randomUUID();

        when(userService.getUserById(mockId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/by-id/" + mockId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldReturn200_whenUpdateDtoCorrect_andUserFound() throws Exception {
        String jsonUpdate =
                """
                        {
                            "username":"updated_name",
                            "email":"updated@gmail.com",
                            "rawPassword": ""
                        }
                        """;

        UserUpdateDto updatedDto = new UserUpdateDto(
                "updated_name",
                "updated@gmail.com",
                "");
        UUID mockId = UUID.randomUUID();

        when(userService.updateUser(mockId, updatedDto)).thenReturn(new UserReadDto(
                mockId, updatedDto.getUsername(), updatedDto.getEmail(), LocalDate.now()));

        mockMvc.perform(put("/user/update-user/" + mockId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updatedDto.getUsername()))
                .andExpect(jsonPath("$.email").value(updatedDto.getEmail()))
                .andExpect(jsonPath("$.rawPassword").doesNotExist());
    }

    @Test
    void updateUser_shouldReturn400_whenAllFieldsEmpty() throws Exception {
        String jsonUpdate = """
                {
                    "username": "",
                    "email": "",
                    "rawPassword": ""
                }
                """;

        UUID mockId = UUID.randomUUID();

        mockMvc.perform(put("/user/update-user/" + mockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray());

    }

    @Test
    void deleteUser_shouldReturn200_whenPasswordCorrect_andUserExists() throws Exception {
        UUID mockId = UUID.randomUUID();
        String rawPassword = "correct_password";

        doNothing().when(userService).deleteUser(mockId, rawPassword);

        mockMvc.perform(delete("/user/delete-user/" + mockId)
                        .param("rawPassword", rawPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }
    @Test
    void deleteUser_shouldReturn404_whenUserNotFound() throws Exception {
        UUID mockId = UUID.randomUUID();
        String rawPassword = "any_password";

        doThrow(new EntityNotFoundException("Пользователь не найден"))
                .when(userService).deleteUser(mockId, rawPassword);

        mockMvc.perform(delete("/user/delete-user/" + mockId)
                        .param("rawPassword", rawPassword))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }
    @Test
    void deleteUser_shouldReturn403_whenPasswordIncorrect() throws Exception {
        UUID mockId = UUID.randomUUID();
        String rawPassword = "wrong_password";

        doThrow(new SecurityException("Неверный пароль"))
                .when(userService).deleteUser(mockId, rawPassword);

        mockMvc.perform(delete("/user/delete-user/" + mockId)
                        .param("rawPassword", rawPassword))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Неверный пароль"));
    }
}