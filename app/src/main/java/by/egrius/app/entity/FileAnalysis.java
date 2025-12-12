package by.egrius.app.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name="FileAnalysis")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "uploadedFile")
public class FileAnalysis {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fileId", unique = true, nullable = false)
    private UploadedFile uploadedFile;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "starts_with_map", joinColumns = @JoinColumn(name = "fileId"))
    @MapKeyColumn(name = "prefix")
    @Column(name = "count")
    private Map<Character, Long> startsWithMap;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "punctuation_stats", joinColumns = @JoinColumn(name="fileId"))
    @MapKeyColumn(name = "character")
    @Column(name = "count")
    private Map<Character, Long> punctuationMap;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "top_words", joinColumns = @JoinColumn(name = "fileId"))
    @MapKeyColumn(name = "word")
    @Column(name = "count")
    private Map<String, Long> topWords;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "words_length", joinColumns = @JoinColumn(name="fileId"))
    @MapKeyColumn(name = "topWord")
    @Column(name = "count")
    private Map<String, Integer> wordLengthMap;

    @Column(nullable = false)
    private Boolean stopWordsExcluded;

}
