package by.egrius.app.entity;

import by.egrius.app.entity.enums.PatternType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.MapKeyType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "RegexMatch")
@NoArgsConstructor
@Builder
@Getter
@Setter
@AllArgsConstructor
public class RegexMatch {
    @Id
    @GeneratedValue
    @Column(name = "regexId")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fileId", unique = true, nullable = false)
    private UploadedFile uploadedFile;

    @OneToMany(mappedBy = "regexMatch", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PatternMatches> patternMatches;

    Long totalMatches;
}
