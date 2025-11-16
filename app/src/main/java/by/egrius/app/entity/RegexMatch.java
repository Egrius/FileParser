package by.egrius.app.entity;

import by.egrius.app.entity.enums.PatternType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "RegexMatch",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fileId", "patternType"})
)
@NoArgsConstructor
@Builder
@Getter
@Setter
@AllArgsConstructor
public class RegexMatch {
    @Id
    @Column(name = "fileId")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "fileId")
    private UploadedFile uploadedFile;


    @Enumerated(value = EnumType.STRING)
    private PatternType patternType;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name="regex_matches", joinColumns = @JoinColumn(name = "fileId"))
    @MapKeyColumn(name = "patternType")
    @Column(name="match")
    private Map<PatternType, List<String>> matchesByType;

    Long totalMatches;
}
