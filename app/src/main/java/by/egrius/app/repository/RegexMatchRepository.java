package by.egrius.app.repository;

import by.egrius.app.entity.RegexMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

public interface RegexMatchRepository extends JpaRepository<RegexMatch, UUID> {
    Optional<RegexMatch> findByUploadedFileId(@RequestParam("fileId") UUID fileId);
}
