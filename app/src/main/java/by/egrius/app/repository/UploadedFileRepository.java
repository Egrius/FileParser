package by.egrius.app.repository;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Page<UploadedFile> findAllByUser_UserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<UploadedFile> findByIdAndUser_UserId(UUID fileId, UUID userId);

}
