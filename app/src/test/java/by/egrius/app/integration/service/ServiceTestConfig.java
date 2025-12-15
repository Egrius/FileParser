package by.egrius.app.integration.service;

import by.egrius.app.mapper.RegexMatchReadMapper;
import by.egrius.app.mapper.fileMapper.FileAnalysisReadMapper;
import by.egrius.app.mapper.fileMapper.UploadedFileReadMapper;
import by.egrius.app.mapper.userMapper.UserCreateMapper;
import by.egrius.app.mapper.userMapper.UserReadMapper;
import by.egrius.app.mapper.userMapper.UserUpdateMapper;
import by.egrius.app.publisher.FileEventPublisher;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UserService;
import jakarta.validation.Validator;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@TestConfiguration
@Import({
        UserReadMapper.class,
        UserCreateMapper.class,
        UserUpdateMapper.class,
        UploadedFileReadMapper.class,
        FileAnalysisReadMapper.class,
        RegexMatchReadMapper.class
})
public class ServiceTestConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Validator validator() {
        return jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
    }
    /*
    @Bean
    public UserService userService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserReadMapper userReadMapper,
            UserCreateMapper userCreateMapper,
            UserUpdateMapper userUpdateMapper,
            Validator validator
    ) {
        return new UserService(
                userRepository,
                passwordEncoder,
                userReadMapper,
                userCreateMapper,
                userUpdateMapper,
                validator
        );
    }
*/
    @Bean
    @Primary
    public FileEventPublisher fileEventPublisher() {
        return Mockito.mock(FileEventPublisher.class);
    }
}
