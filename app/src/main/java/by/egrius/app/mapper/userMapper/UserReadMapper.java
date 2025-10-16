package by.egrius.app.mapper.userMapper;

import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.entity.User;
import by.egrius.app.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class UserReadMapper implements BaseMapper<User, UserReadDto> {
    @Override
    public UserReadDto map(User object) {
        return new UserReadDto(
                object.getUserId(),
                object.getUsername(),
                object.getEmail(),
                object.getCreatedAt()
        );
    }
}
