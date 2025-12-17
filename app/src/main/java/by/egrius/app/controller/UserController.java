package by.egrius.app.controller;

import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserReadDto> getUserByUsername(@PathVariable("username") String username) {
        Optional<UserReadDto> user = userService.getUserByUsername(username);
        return ResponseEntity.of(user);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<UserReadDto> getUserByUserId(@PathVariable("id") UUID id) {
        Optional<UserReadDto> user = userService.getUserById(id);
        return ResponseEntity.of(user);
    }

    @PostMapping("/create-user")
    public ResponseEntity<UserReadDto> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {

        UserReadDto createdUserReadDto = userService.createUser(userCreateDto);
        return ResponseEntity.ok(createdUserReadDto);
    }

    @PutMapping("/update-user/{id}")
    public ResponseEntity<UserReadDto> updateUser(@PathVariable UUID id,
                                                  @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserReadDto updatedUserReadDto = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ofNullable(updatedUserReadDto);
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable UUID id,
                                                           @RequestParam String rawPassword) {
        userService.deleteUser(id, rawPassword);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
