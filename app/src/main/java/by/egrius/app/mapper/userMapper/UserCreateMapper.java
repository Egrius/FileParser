package by.egrius.app.mapper.userMapper;

import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.entity.User;
import by.egrius.app.mapper.BaseMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@Component
public class UserCreateMapper implements BaseMapper<UserCreateDto, User> {

    @Override
    public User map(UserCreateDto object) {
        User user = new User();
        copy(object, user);
        return user;

    }

    @Override
    public User map(UserCreateDto fromObject, User toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    private void copy(UserCreateDto object, User user) {
        //user.setUserId(UUID.randomUUID());
        user.setUsername(object.getUsername());
        user.setEmail(object.getEmail());
        user.setFiles(Collections.emptyList());
        user.setCreatedAt(LocalDate.now());
    }
}
