package by.egrius.app.repository;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {
    Page<UploadedFileReadDto> findAllByUser_UserId(UUID userId, Pageable pageable);


}
