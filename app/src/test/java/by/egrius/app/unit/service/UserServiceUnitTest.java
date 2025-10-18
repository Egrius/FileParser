package by.egrius.app.unit.service;

import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.entity.User;
import by.egrius.app.mapper.fileMapper.UploadedFileReadMapper;
import by.egrius.app.mapper.userMapper.UserCreateMapper;
import by.egrius.app.mapper.userMapper.UserReadMapper;
import by.egrius.app.mapper.userMapper.UserUpdateMapper;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserReadMapper userReadMapper;

    @Mock
    private UserCreateMapper userCreateMapper;

    @Mock
    private UserUpdateMapper userUpdateMapper;

    @Mock
    private UploadedFileReadMapper fileReadMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserByUsername() {
        UUID id = UUID.randomUUID();
        String username = "TestName";

        User userToReturn = User.builder()
                .userId(id)
                .username(username)
                .email("test@example.com")
                .password("encoded")
                .createdAt(LocalDate.now())
                .files(Collections.emptyList())
                .build();

        UserReadDto dtoToReturn = new UserReadDto(id, username, "test@example.com", LocalDate.now());

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(userToReturn));
        when(userReadMapper.map(any(User.class))).thenReturn(dtoToReturn);

        Optional<UserReadDto> actualResult = userService.getUserByUsername(username);

        assertTrue(actualResult.isPresent());

        assertEquals(actualResult.get().userId(), dtoToReturn.userId());
        assertEquals(actualResult.get().username(), dtoToReturn.username());
    }

    @Test
    void getUserById() {
        UUID id = UUID.randomUUID();
        String username = "TestName";

        User userToReturn = User.builder()
                .userId(id)
                .username(username)
                .email("test@example.com")
                .password("encoded")
                .createdAt(LocalDate.now())
                .files(Collections.emptyList())
                .build();

        UserReadDto dtoToReturn = new UserReadDto(id, username, "test@example.com", LocalDate.now());

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userToReturn));
        when(userReadMapper.map(any(User.class))).thenReturn(dtoToReturn);

        Optional<UserReadDto> actualResult = userService.getUserById(id);

        assertTrue(actualResult.isPresent());

        assertEquals(actualResult.get().userId(), dtoToReturn.userId());
        assertEquals(actualResult.get().username(), dtoToReturn.username());
    }

    @Test
    void createUser_shouldEncodePasswordAndSaveUser() {
        UserCreateDto dto = new UserCreateDto("Ivan", "ivan@example.com", "1234");
        User user = new User();

        user.setUsername("Ivan");
        user.setEmail("ivan@example.com");
        user.setPassword("encoded");

        UserReadDto readDto = new UserReadDto(null,"Ivan", "ivan@example.com", null);

        when(userReadMapper.map(user)).thenReturn(readDto);
        when(userCreateMapper.map(dto)).thenReturn(user);
        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserReadDto result = userService.createUser(dto);

        assertEquals("Ivan", result.username());
        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser() {
        UUID id = UUID.randomUUID();

        String updatedUsername = "Updated_TestName";
        String updatedEmail = "updated_test@gmail.com";
        String updatedPassword = "updated_encoded";

        String username = "TestName";

        User beforeUpdateUser = User.builder()
                .userId(id)
                .username(username)
                .email("test@example.com")
                .password("encoded")
                .createdAt(LocalDate.now())
                .files(Collections.emptyList())
                .build();

        User afterUpdateUser = User.builder()
                .userId(id)
                .username(updatedUsername)
                .email(updatedEmail)
                .password(updatedPassword)
                .createdAt(LocalDate.now())
                .files(Collections.emptyList())
                .build();

        UserUpdateDto updateDto = new UserUpdateDto(updatedUsername, updatedEmail, updatedPassword);

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(beforeUpdateUser));
        when(userUpdateMapper.map(any(UserUpdateDto.class), any(User.class))).thenReturn(afterUpdateUser);
        when(userReadMapper.map(any(User.class))).thenReturn(new UserReadDto(id, updateDto.getUsername(), updateDto.getUsername(), LocalDate.now()));


        UserReadDto actualResult = userService.updateUser(id, updateDto);

        verify(userRepository).save(any(User.class));

        assertNotNull(actualResult);

        assertNotEquals(beforeUpdateUser.getUsername(), actualResult.username());
        assertNotEquals(beforeUpdateUser.getEmail(), actualResult.email());
    }

    @Test
    void deleteUser() {
        UUID id = UUID.randomUUID();
        String password = "encoded";

        User userToDelete = User.builder()
                .userId(id)
                .username("Test")
                .email("test@example.com")
                .password("encoded")
                .createdAt(LocalDate.now())
                .files(Collections.emptyList())
                .build();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userToDelete));
        when(passwordEncoder.matches(eq("1234"), eq(userToDelete.getPassword()))).thenReturn(true);

        userService.deleteUser(id, "1234");

        verify(userRepository).deleteById(eq(id));
    }
}