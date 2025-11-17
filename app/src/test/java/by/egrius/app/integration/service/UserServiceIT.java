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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    void getUserByUsername_ShouldReturnCorrectUser() {
        UUID idToFindUser = UUID.randomUUID();
        String nameToFindUser = "TestToFind";

        User userToFind = User.builder()
                .userId(idToFindUser)
                .username(nameToFindUser)
                .email("delete@gmail.com")
                .password(passwordEncoder.encode("123"))
                .createdAt(LocalDate.now())
                .build();

        userRepository.save(userToFind);

        Optional<UserReadDto> found = userService.getUserByUsername(nameToFindUser);

        assertTrue(found.isPresent());

        assertEquals(found.get().userId(), idToFindUser);
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
    void updateUser_ShouldChangeDatabaseEntity() {
        String nameToFindUser = "testuser";
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
    void delete_ShouldDeleteUserFromDatabase() {

        User user = userRepository.findByUsername("TestUserToUploadFile_2").orElseThrow(
                () -> new EntityNotFoundException("Not found a user to remove"));

        UUID idToDelete = user.getUserId();

        userService.deleteUser(idToDelete, "1234");

        Optional<User> deletedUser = userRepository.findById(idToDelete);
        assertTrue(deletedUser.isEmpty());
    }
}