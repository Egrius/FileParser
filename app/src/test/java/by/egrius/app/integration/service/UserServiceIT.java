package by.egrius.app.integration.service;

import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.entity.User;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceIT {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void getUserByUsername_ShouldReturnCorrectUser() {
        String nameToFindUser = "TestToFind";

        User userToFind = User.builder()
                .username(nameToFindUser)
                .email("delete@gmail.com")
                .password(passwordEncoder.encode("123"))
                .createdAt(LocalDate.now())
                .build();

        userRepository.save(userToFind);
        userRepository.flush();

        Optional<UserReadDto> found = userService.getUserByUsername(nameToFindUser);

        assertTrue(found.isPresent());

        assertEquals(found.get().username(), nameToFindUser);
    }


    @Test
    @Transactional
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
    @Transactional
    void updateUser_withNonExistingId_shouldThrowEntityNotFoundException() {
        UUID nonExistingId = UUID.randomUUID();
        UserUpdateDto updateDto = new UserUpdateDto("NewName", "new@example.com", "newpass");

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(nonExistingId, updateDto));
    }


    @Test
    @Transactional
    void updateUser_ShouldChangeDatabaseEntity() {
        String nameToFindUser = "testuser";
        userRepository.save(new User(null ,nameToFindUser, "testEmail@gmail.com","123", LocalDate.now()));
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

        userService.updateUser(idOfUserToUpdate, updateDto);

        Optional<User> userAfterUpdate = userRepository.findById(idOfUserToUpdate);

        assertTrue(userAfterUpdate.isPresent());

        assertNotEquals(oldUsername, userAfterUpdate.get().getUsername());
        assertNotEquals(oldEmail, userAfterUpdate.get().getEmail());
        assertNotEquals(oldPassword, userAfterUpdate.get().getPassword());
    }

    @Test
    @Transactional
    void updateUser_withoutPassword_shouldKeepOldPassword() {
        User user = userRepository.save(new User(null, "NoPassUser", "nopass@example.com", passwordEncoder.encode("oldpass"), LocalDate.now()));
        UUID id = user.getUserId();

        UserUpdateDto updateDto = new UserUpdateDto("UpdatedName", "updated@example.com", null);
        userService.updateUser(id, updateDto);

        User updated = userRepository.findById(id).orElseThrow();
        assertTrue(passwordEncoder.matches("oldpass", updated.getPassword()));
    }

    @Test
    @Transactional
    void delete_ShouldDeleteUserFromDatabase() {
        String nameToDelete = "TestUserToDelete";
        String emailToDelete = "emailToDelte@gmail.com";
        String rawPasswordToDelete = "1234";
        String encodedPassword = passwordEncoder.encode(rawPasswordToDelete);

        userRepository.save(new User(null, nameToDelete, emailToDelete, encodedPassword, LocalDate.now()));

        User userToDelete = userRepository.findByUsername(nameToDelete).orElseThrow(
                () -> new EntityNotFoundException("Не удалось найти пользователя перед его удалением"));

        UUID idToDelete = userToDelete.getUserId();

        userService.deleteUser(userToDelete.getUserId(), rawPasswordToDelete);

        Optional<User> deletedUser = userRepository.findById(idToDelete);
        assertTrue(deletedUser.isEmpty());
    }

    @Test
    @Transactional
    void deleteUser_withWrongPassword_shouldThrowSecurityException() {
        User user = userRepository.save(new User(null, "UserToDelete", "delete@example.com", passwordEncoder.encode("correct"), LocalDate.now()));

        assertThrows(SecurityException.class, () -> userService.deleteUser(user.getUserId(), "wrong"));
    }

    @Test
    @Transactional
    void deleteUser_withNonExistingId_shouldThrowEntityNotFoundException() {
        UUID nonExistingId = UUID.randomUUID();

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(nonExistingId, "1234"));
    }
}