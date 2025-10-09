package by.egrius.app.entity;

import by.egrius.app.entity.enums.PatternType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name="RegexMatch")
@NoArgsConstructor
@AllArgsConstructor
public class RegexMatch {
    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "fileId")
    private UploadedFile uploadedFile;

    @Enumerated(value = EnumType.STRING)
    private PatternType patternType;

    private List<String> matches;

    private Long matchCount;
}
