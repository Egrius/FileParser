package by.egrius.app;

import by.egrius.app.entity.User;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

public class TestUtils {
    public static User createTestUser(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      String suffix) {
        User user = User.builder()
                .email("test_" + suffix + "@gmail.com")
                .username("TestUser_" + suffix)
                .password(passwordEncoder.encode("1234"))
                .createdAt(LocalDate.now())
                .build();
        return userRepository.save(user);
    }

    public static void setupSecurityContext(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
