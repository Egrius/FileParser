package by.egrius.app.repository;

import by.egrius.app.entity.FileAnalysis;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface FileAnalysisRepository extends JpaRepository<FileAnalysis, UUID> {

    Optional<FileAnalysis> findByUploadedFile_Id(UUID fileId);

    boolean existsByUploadedFile_Id(UUID fileId);

    void deleteByUploadedFile_Id(UUID fileId);
}
