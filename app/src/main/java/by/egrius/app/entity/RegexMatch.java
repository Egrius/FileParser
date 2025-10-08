package by.egrius.app.entity;

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
    UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "fileId")
    UploadedFile uploadedFile;

    PatternType patternType;

    List<String> matches;

    Long matchCount;
}
