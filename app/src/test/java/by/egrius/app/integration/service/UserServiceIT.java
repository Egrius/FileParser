package by.egrius.app.integration.service;

import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.entity.User;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("service-test")
@Import({
        ServiceTestConfig.class,
        UserService.class
})
class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void getUserByUsername_ShouldReturnCorrectUser() {
        String nameToFindUser = "TestToFind";

        User userToFind = User.builder()
                .username(nameToFindUser)
                .email("delete@gmail.com")
                .password(passwordEncoder.encode("123"))
                .createdAt(LocalDate.now())
                .build();

        userRepository.save(userToFind);

        Optional<UserReadDto> found = userService.getUserByUsername(nameToFindUser);

        assertTrue(found.isPresent());
        assertEquals(found.get().username(), nameToFindUser);
    }

    @Test
    void createUser_shouldPersistUserAndReturnDto() {

        UserCreateDto dto = new UserCreateDto("Aleksey", "alex@example.com", "1234");

        UserReadDto result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("Aleksey", result.username());

        Optional<User> saved = userRepository.findByUsername("Aleksey");
        assertTrue(saved.isPresent());
        assertNotEquals("1234", saved.get().getPassword());
    }

    @Test
    void updateUser_withNonExistingId_shouldThrowEntityNotFoundException() {

        UUID nonExistingId = UUID.randomUUID();
        UserUpdateDto updateDto = new UserUpdateDto("NewName", "new@example.com", "newpass");

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(nonExistingId, updateDto));
    }

    @Test
    void updateUser_ShouldChangeDatabaseEntity() {
        // Arrange
        String nameToFindUser = "testuser";

        // Используем Builder вместо конструктора
        User user = User.builder()
                .username(nameToFindUser)
                .email("testEmail@gmail.com")
                .password("123")
                .createdAt(LocalDate.now())
                .build();

        User savedUser = userRepository.save(user);

        Optional<User> userToUpdate = userRepository.findByUsername(nameToFindUser);
        assertTrue(userToUpdate.isPresent());

        String oldUsername = userToUpdate.get().getUsername();
        String oldEmail = userToUpdate.get().getEmail();
        String oldPassword = userToUpdate.get().getPassword();

        UUID idOfUserToUpdate = userToUpdate.get().getUserId();
        String updatedUsername = "Updated_TestName";
        String updatedEmail = "updated_test@gmail.com";
        String updatedPassword = "updated_encoded";

        UserUpdateDto updateDto = new UserUpdateDto(updatedUsername, updatedEmail, updatedPassword);

        // Act
        userService.updateUser(idOfUserToUpdate, updateDto);

        // Assert
        Optional<User> userAfterUpdate = userRepository.findById(idOfUserToUpdate);
        assertTrue(userAfterUpdate.isPresent());

        User updatedUser = userAfterUpdate.get();
        assertNotEquals(oldUsername, updatedUser.getUsername());
        assertNotEquals(oldEmail, updatedUser.getEmail());
        // Пароль должен быть зашифрованным в сервисе
        assertNotEquals(oldPassword, updatedUser.getPassword());
    }

    @Test
    void updateUser_withoutPassword_shouldKeepOldPassword() {

        String oldPassword = "oldpass";

        User user = User.builder()
                .username("NoPassUser")
                .email("nopass@example.com")
                .password(passwordEncoder.encode(oldPassword))
                .createdAt(LocalDate.now())
                .build();

        User savedUser = userRepository.save(user);
        UUID id = savedUser.getUserId();

        UserUpdateDto updateDto = new UserUpdateDto("UpdatedName", "updated@example.com", null);

        userService.updateUser(id, updateDto);

        User updated = userRepository.findById(id).orElseThrow();
        assertTrue(passwordEncoder.matches(oldPassword, updated.getPassword()));
    }

    @Test
    void delete_ShouldDeleteUserFromDatabase() {

        String nameToDelete = "TestUserToDelete";
        String emailToDelete = "emailToDelete@gmail.com";
        String rawPasswordToDelete = "1234";

        User user = User.builder()
                .username(nameToDelete)
                .email(emailToDelete)
                .password(passwordEncoder.encode(rawPasswordToDelete))
                .createdAt(LocalDate.now())
                .build();

        User savedUser = userRepository.save(user);

        User userToDelete = userRepository.findByUsername(nameToDelete).orElseThrow(
                () -> new EntityNotFoundException("Не удалось найти пользователя перед его удалением"));

        UUID idToDelete = userToDelete.getUserId();

        userService.deleteUser(userToDelete.getUserId(), rawPasswordToDelete);

        Optional<User> deletedUser = userRepository.findById(idToDelete);
        assertTrue(deletedUser.isEmpty());
    }

    @Test
    void deleteUser_withWrongPassword_shouldThrowSecurityException() {

        String correctPassword = "correct";

        User user = User.builder()
                .username("UserToDelete")
                .email("delete@example.com")
                .password(passwordEncoder.encode(correctPassword))
                .createdAt(LocalDate.now())
                .build();

        User savedUser = userRepository.save(user);
        UUID userId = savedUser.getUserId();

        assertThrows(SecurityException.class,
                () -> userService.deleteUser(userId, "wrong"));
    }

    @Test
    void deleteUser_withNonExistingId_shouldThrowEntityNotFoundException() {

        UUID nonExistingId = UUID.randomUUID();

        assertThrows(EntityNotFoundException.class,
                () -> userService.deleteUser(nonExistingId, "1234"));
    }
}