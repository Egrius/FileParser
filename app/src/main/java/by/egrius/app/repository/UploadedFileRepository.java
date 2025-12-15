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

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    @Query("SELECT f from UploadedFile f WHERE f.user.userId = :userId")
    Page<UploadedFile> findAllFilesByUserId(@Param("userId") UUID userId,
                                            Pageable pageable);

    @Query("SELECT f FROM UploadedFile f JOIN FETCH f.user u WHERE f.filename = :filename AND u.userId = :userId")
    Optional<UploadedFile> findByFilenameAndUserId(@Param("filename") String filename,
                                                   @Param("userId") UUID userId);

    @Query("SELECT f FROM UploadedFile f JOIN FETCH f.user u WHERE f.id = :fileId AND u.userId = :userId")
    Optional<UploadedFile> findByFileIdAndUserId(@Param("fileId") UUID fileId,
                                                 @Param("userId") UUID userId);

    @Query("SELECT f FROM UploadedFile f JOIN FETCH f.user u JOIN FETCH f.fileContent c WHERE f.id = :fileId AND u.userId = :userId")
    Optional<UploadedFile> findByIdWithUserAndContent(@Param("fileId") UUID fileId,
                                                      @Param("userId") UUID userId);

    // @EntityGraph(attributePaths = {"fileAnalysis"})
    @Query("SELECT f FROM UploadedFile f LEFT JOIN FETCH f.fileAnalysis WHERE f.id = :id")
    Optional<UploadedFile> findWithFileAnalysisById(@Param("id") UUID id);

}