package by.egrius.app.integration.controller;

import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.entity.User;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USERNAME = "TestUser";
    private static final String TEST_PASSWORD = "1234";
    private static final String TEST_EMAIL = "test@test.com";

    @BeforeEach
    void clearDatabase() {
        userRepository.deleteAll();
    }

    @Nested
    class GetUserTests {

        private UUID existingUserId;

        @BeforeEach
        void setUpOneUser() {

            UserCreateDto existing = new UserCreateDto(
                    "ExistingUser", "existing@test.com", "1234"
            );
            userService.createUser(existing);
            existingUserId = userRepository.findByUsername("ExistingUser").get().getUserId();
        }

        @Test
        void getUserByUsername_shouldReturnExistingUser() throws Exception {
            mockMvc.perform(get("/user/by-username/ExistingUser")
                            .header("Authorization",
                                    "Basic " + Base64.getEncoder().encodeToString("ExistingUser:1234".getBytes())))
                    .andExpect(status().isOk());
        }

        @Test
        void getUserByUsername_shouldReturn404WhenUserNotFound()  throws Exception {
            mockMvc.perform(get("/user/by-username/Non-ExistingUser")
                            .with(httpBasic("ExistingUser", "1234")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getUserById_shouldReturnExistingUser() throws Exception {
            mockMvc.perform(get("/user/by-id/" +  existingUserId.toString())
                            .with(httpBasic("ExistingUser", "1234")))
                    .andExpect(status().isOk());
        }

        @Test
        void getUserById_shouldReturn404WhenUserNotFound()  throws Exception {
            mockMvc.perform(get("/user/by-id/" + UUID.randomUUID().toString())
                            .with(httpBasic("ExistingUser", "1234")))
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    class CreateUserTests {

        @Test
        void shouldCreateUserSuccessfully() throws Exception {

            String username  = "Alexey";
            String email = "alexey@gmail.com";

            String jsonToCreate =
                    """
                        {
                            "username":"Alexey",
                            "email":"alexey@gmail.com",
                            "rawPassword":"12345"
                        }
                    """;

            mockMvc.perform(post("/user/create-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonToCreate))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.userId").isNotEmpty());
        }

        @Test
        void shouldThrowIllegalStateException_whenCreateUserExists() throws Exception {

            UserCreateDto existingUser = new UserCreateDto(
                    "ExistingUser",
                    "existing@test.com",
                    "pass123"
            );
            userService.createUser(existingUser);

            String jsonToCreate =
                    """
                        {
                            "username":"ExistingUser",
                            "email":"existing@test.com",
                            "rawPassword":"12345"
                        }
                    """;

            mockMvc.perform(post("/user/create-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonToCreate))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Пользователь с таким именем уже существует"))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.path").value("/user/create-user"))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
        }

        @Test
        void shouldReturn400WhenInvalidEmail() throws Exception {
                String json = """
            {
                "username": "ValidUser",
                "email": "invalid-email",
                "rawPassword": "12345"
            }
            """;

            mockMvc.perform(post("/user/create-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[0].field").value("email"));
        }

        @Test
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            String json = """
            {
                "username": "ValidUser",
                "email": "valid@email.com",
                "rawPassword": "123"
            }
            """;

            mockMvc.perform(post("/user/create-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[0].field").value("rawPassword"));
        }
    }

    @Nested
    class UpdateUserTests {

        private UUID testUserUUID;
        private UUID existingUUID;

        @BeforeEach
        void setUpTwoUsers() {

            UserCreateDto existing = new UserCreateDto(
                    "ExistingUser", "existing@test.com", "pass123"
            );
            userService.createUser(existing);
            existingUUID = userRepository.findByUsername("ExistingUser").get().getUserId();

            UserCreateDto testUser = new UserCreateDto(
                    "TestUser", "test@test.com", "1234"
            );
            userService.createUser(testUser);
            testUserUUID = userRepository.findByUsername("TestUser").get().getUserId();
        }

        @Test
        void updateUser_shouldReturnDtoIfCorrect() throws Exception{
            String validJson = """
            {
                "username": "UpdatedName",
                "email": "updated@email.com"
            }
        """;

            mockMvc.perform(put("/user/update-user/" + testUserUUID.toString())
                            .with(httpBasic("TestUser", "1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("UpdatedName"))
                    .andExpect(jsonPath("$.email").value("updated@email.com"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.userId").value(testUserUUID.toString()));

            User updatedUser = userRepository.findById(testUserUUID).orElseThrow();
            assertEquals("UpdatedName", updatedUser.getUsername());
            assertEquals("updated@email.com", updatedUser.getEmail());
        }

        @Test
        void updateUser_shouldUpdatePassword() throws Exception {
            String json = """
            {
                "username": "UpdatedName",
                "email": "updated@email.com",
                "rawPassword": "newPassword123"
            }
            """;

            mockMvc.perform(put("/user/update-user/" + testUserUUID)
                            .with(httpBasic("TestUser", "1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/user/by-id/" + testUserUUID)
                            .with(httpBasic("UpdatedName", "newPassword123")))
                    .andExpect(status().isOk());
        }

        @Test
        void updateUser_shouldReturn403WhenTryingToUpdateDifferentUser() throws Exception {

            String validJson = """
            {
                "username": "UpdatedName",
                "email": "updated@email.com"
            }
        """;

            mockMvc.perform(put("/user/update-user/" + existingUUID.toString())
                            .with(httpBasic("TestUser", "1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJson))
                    .andExpect(status().isForbidden());
        }

        @Test
        void updateUser_shouldReturn400WhenInvalidData() throws Exception {
            String invalidJson = """
            {
                "username": "UpdatedName",
                "email": "badEmail.com"
            }
        """;

            mockMvc.perform(put("/user/update-user/" + testUserUUID.toString())
                            .with(httpBasic("TestUser", "1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[0].field").value("email"))
                    .andExpect(jsonPath("$.violations[0].msg").value("Email должен быть корректным"));

        }

        @Test
        void updateUser_shouldReturn404WhenUserNotFound() throws Exception {
            String invalidJson = """
            {
                "username": "UpdatedName",
                "email": "exampleUpdated@gmail.com"
            }
        """;

            String randomUUID =  UUID.randomUUID().toString();

            mockMvc.perform(put("/user/update-user/" + randomUUID)
                            .with(httpBasic("TestUser", "1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Пользователь для обновления не найден"))
                    .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND_ERROR"))
                    .andExpect(jsonPath("$.path").value("/user/update-user/" + randomUUID))
                    .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
        }

        @Test
        void updateUser_shouldReturn400WhenUsernameAlreadyTaken() throws Exception {
            String json = """
            {
                "username": "ExistingUser",
                    "email": "test@test.com"
            }
            """;

                mockMvc.perform(put("/user/update-user/" + testUserUUID)
                                .with(httpBasic("TestUser", "1234"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Данное имя пользователя уже занято"));
            }

            @Test
            void updateUser_shouldReturn400WhenEmailAlreadyTaken() throws Exception {
                String json = """
            {
                "username": "TestUser",
                "email": "existing@test.com"
            }
            """;

            mockMvc.perform(put("/user/update-user/" + testUserUUID)
                            .with(httpBasic("TestUser", "1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Данный email уже используется"));
        }
    }
}