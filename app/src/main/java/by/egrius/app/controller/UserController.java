package by.egrius.app.controller;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//                          ДОБАВИТЬ ВАЛИДАЦИЮ ДТО

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("by-username/{username}")
    public ResponseEntity<UserReadDto> getUserByUsername(@PathVariable("username") String username) {
        Optional<UserReadDto> user = userService.getUserByUsername(username);
        return ResponseEntity.of(user);
    }

    @GetMapping("by-id/{id}")
    public ResponseEntity<UserReadDto> getUserByUserId(@PathVariable("id") UUID id) {
        Optional<UserReadDto> user = userService.getUserById(id);
        return ResponseEntity.of(user);
    }

    @GetMapping("/user-files/{id}")
    public ResponseEntity<List<UploadedFileReadDto>> getUserFilesById(@PathVariable("id") UUID id) {
        List<UploadedFileReadDto> files = userService.getUploadedUserFilesById(id);
        return ResponseEntity.ofNullable(files);
    }

    @PostMapping("/create-user")
    public ResponseEntity<UserReadDto> createUser(@RequestBody UserCreateDto userCreateDto) {
        if(userCreateDto.getUsername().isEmpty()) {
            throw new SecurityException("Имя пользователя не должно быть пустым");
        }
        if(userCreateDto.getEmail().isEmpty()) {
            throw new SecurityException("Почта пользователя не должно быть пустым");
        }
        if(userCreateDto.getRawPassword().length() < 4) {
            throw new SecurityException("Пароль должен быть длиннее 4 символов");
        }

        UserReadDto createdUserReadDto = userService.createUser(userCreateDto);
        return ResponseEntity.ofNullable(createdUserReadDto);
    }

    @PostMapping("/update-user/{id}")
    public ResponseEntity<UserReadDto> updateUser(@PathVariable UUID id,
                                                  @RequestBody UserUpdateDto userUpdateDto) {
        // Сделать валидацию дтошки без if-ов

        if(userUpdateDto.getUsername().isEmpty()) {
            throw new SecurityException("Имя пользователя не должно быть пустым");
        }
        if(userUpdateDto.getEmail().isEmpty()) {
            throw new SecurityException("Почта пользователя не должно быть пустым");
        }
        if(userUpdateDto.getRawPassword().length() < 4) {
            throw new SecurityException("Пароль должен быть длиннее 4 символов");
        }

        UserReadDto updatedUserReadDto = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ofNullable(updatedUserReadDto);

    }

    @DeleteMapping("/delete-user/{id}")
    public Map<String, Boolean> deleteUser(@PathVariable UUID id,
                                           @RequestParam String rawPassword) {

        if(id == null) throw new IllegalArgumentException("ID должен быть не пустым");
        if(rawPassword.isEmpty()) throw new IllegalArgumentException("Пароль должен быть не пустым");

        userService.deleteUser(id, rawPassword);

        return Map.of("deleted", true);
    }
}
