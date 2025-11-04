package by.egrius.app.service;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.entity.User;
import by.egrius.app.mapper.fileMapper.UploadedFileReadMapper;
import by.egrius.app.mapper.userMapper.UserCreateMapper;
import by.egrius.app.mapper.userMapper.UserReadMapper;
import by.egrius.app.mapper.userMapper.UserUpdateMapper;
import by.egrius.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserReadMapper userReadMapper;
    private final  UserCreateMapper userCreateMapper;
    private final UserUpdateMapper userUpdateMapper;
    private final UploadedFileReadMapper fileReadMapper;

    private final Validator validator;

    public Optional<UserReadDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(userReadMapper::map);
    }

    public Optional<UserReadDto> getUserById(UUID id) {
        return userRepository.findById(id).map(userReadMapper::map);
    }

    public List<UploadedFileReadDto> getUploadedUserFilesById(UUID id) {
        return userRepository.findById(id)
                .map(
                        user ->  user.getFiles()
                                .stream()
                                .map(fileReadMapper::map)
                                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Transactional
    public UserReadDto createUser(UserCreateDto userCreateDto) {
        User user = userCreateMapper.map(userCreateDto);
        user.setPassword(passwordEncoder.encode(userCreateDto.getRawPassword()));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        userRepository.save(user);
        return userReadMapper.map(user);
    }

    @Transactional
    public UserReadDto updateUser(UUID id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь для обновления не найден"));

        userUpdateMapper.map(userUpdateDto, user);

        if (userUpdateDto.getRawPassword() != null && !userUpdateDto.getRawPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.getRawPassword()));
        }

        userRepository.save(user);
        userRepository.flush();
        return userReadMapper.map(user);
    }

    @Transactional
    public void deleteUser(UUID id, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            userRepository.deleteById(id);
        } else {
            throw new SecurityException("Invalid password");
        }
    }
}
