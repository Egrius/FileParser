package by.egrius.app.repository;

import by.egrius.app.entity.PatternMatches;
import by.egrius.app.entity.enums.PatternType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PatternMatchesRepository extends JpaRepository<PatternMatches, UUID> {

    @Query("SELECT pm FROM PatternMatches pm " +
            "WHERE pm.fileId = :fileId " +
            "AND pm.patternType = :patternType")
    List<PatternMatches> findByRegexMatchUploadedFileIdAndPatternType(@Param("fileId") UUID fileId,
                                                                      @Param("patternType") PatternType type);
}
