package by.egrius.app.repository;

import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID id);


}
