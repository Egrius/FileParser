package by.egrius.app.repository;

import by.egrius.app.entity.RegexMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RegexMatchRepository extends JpaRepository<RegexMatch, UUID> {
}
