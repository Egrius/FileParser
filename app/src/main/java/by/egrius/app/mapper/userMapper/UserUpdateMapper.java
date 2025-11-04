package by.egrius.app.mapper.userMapper;

import by.egrius.app.dto.userDTO.UserUpdateDto;
import by.egrius.app.entity.User;
import by.egrius.app.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class UserUpdateMapper implements BaseMapper<UserUpdateDto, User> {

    @Override
    public User map(UserUpdateDto object) {
        throw new UnsupportedOperationException("Use map(fromObject, toObject) for updates");
    }

    @Override
    public User map(UserUpdateDto fromObject, User toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    private void copy(UserUpdateDto object, User user) {
        if (object.getUsername() != null && !object.getUsername().isBlank()) {
            user.setUsername(object.getUsername());
        }

        if (object.getEmail() != null && !object.getEmail().isBlank()) {
            user.setEmail(object.getEmail());
        }
    }
}