package by.egrius.app.repository;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Page<UploadedFile> findAllByUser_UserId(UUID userId, Pageable pageable);

    @Query("SELECT f FROM UploadedFile f WHERE f.filename = :filename AND f.user.userId = :userId")
    Optional<UploadedFile> findByFilenameAndUserId(@Param("filename") String filename,
                                                   @Param("userId") UUID userId);



    @EntityGraph(attributePaths = {"fileContent", "fileAnalysis", "regexMatch", "fileEventLog"})
    Optional<UploadedFile> findByIdAndUser_UserId(UUID fileId, UUID userId);

}