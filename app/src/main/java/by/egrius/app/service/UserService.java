package by.egrius.app.service;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.entity.User;
import by.egrius.app.mapper.userMapper.UserCreateMapper;
import by.egrius.app.mapper.userMapper.UserReadMapper;
import by.egrius.app.mapper.userMapper.UserUpdateMapper;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserReadMapper userReadMapper;
    private final  UserCreateMapper userCreateMapper;
    private final UserUpdateMapper userUpdateMapper;

    private final Validator validator;

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new SecurityException("Пользователь не аутентифицирован");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Загрузка пользователя с username: {}", username);
        return userRepository.findByUsername(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    public User getCurrentUser() {
        return getCurrentUserPrincipal().getUser();
    }

    public boolean canModifyUser(UUID targetUserId) {
        UserPrincipal principal = getCurrentUserPrincipal();

        return principal.getId().equals(targetUserId);

    }

    public Optional<UserReadDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(userReadMapper::map);
    }

    public Optional<UserReadDto> getUserById(UUID id) {
        return userRepository.findById(id).map(userReadMapper::map);
    }

    @Transactional
    public UserReadDto createUser(UserCreateDto userCreateDto) {

        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = userCreateMapper.map(userCreateDto);
        user.setPassword(passwordEncoder.encode(userCreateDto.getRawPassword()));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        userRepository.save(user);
        log.info("Пользователь создан: username={}, email={}, id={}",
                user.getUsername(), user.getEmail(), user.getUserId());
        return userReadMapper.map(user);
    }

    @Transactional
    public UserReadDto updateUser(UUID id, UserUpdateDto userUpdateDto) {

        if (!canModifyUser(id)) {
            throw new AccessDeniedException("Нет прав для обновления пользователя");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь для обновления не найден"));

        if (userUpdateDto.getUsername() != null &&
                !userUpdateDto.getUsername().equals(user.getUsername()) &&
                userRepository.existsByUsername(userUpdateDto.getUsername())) {
            throw new IllegalArgumentException("Данное имя пользователя уже занято");
        }

        if (userUpdateDto.getEmail() != null &&
                !userUpdateDto.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userUpdateDto.getEmail())) {
            throw new IllegalArgumentException("Данный email уже используется");
        }

        userUpdateMapper.map(userUpdateDto, user);

        if (userUpdateDto.getRawPassword() != null && !userUpdateDto.getRawPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.getRawPassword()));
        }

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        log.info("Пользователь обновлён: {}", user.getUsername());
        return userReadMapper.map(user);
    }

    @Transactional
    public void deleteUser(UUID id, String rawPassword) {

        if (!canModifyUser(id)) {
            throw new AccessDeniedException("Нет прав для обновления пользователя");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new SecurityException("Некорректный пароль");
        }

        userRepository.delete(user);
        log.info("Пользователь '{}' (ID: {}) удалён", user.getUsername(), id);
    }
}
