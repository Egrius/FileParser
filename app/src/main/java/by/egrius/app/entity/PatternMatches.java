package by.egrius.app.entity;

import by.egrius.app.entity.enums.PatternType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.WithBy;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pattern_matches")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PatternMatches {

    @Id
    @GeneratedValue
    @Column(name = "matchId")
    private UUID matchId;

    private PatternType patternType;

    private String match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regexId", nullable = false)
    private RegexMatch regexMatch;
}
