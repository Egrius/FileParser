package by.egrius.app.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name="FileAnalysis")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "uploadedFile")
public class FileAnalysis {
    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "fileId")
    private UploadedFile uploadedFile;

    @ElementCollection
    @CollectionTable(name = "starts_with_map", joinColumns = @JoinColumn(name = "fileId"))
    @MapKeyColumn(name = "prefix")
    @Column(name = "count")
    private Map<String, Long> startsWithMap;

    @ElementCollection
    @CollectionTable(name = "punctuation_stats", joinColumns = @JoinColumn(name="fileId"))
    @MapKeyColumn(name = "character")
    @Column(name = "count")
    private Map<Character, Long> punctuationMap;

    @ElementCollection
    @CollectionTable(name = "top_words", joinColumns = @JoinColumn(name = "fileId"))
    @Column(name = "word")
    private List<String> topWords;

    @ElementCollection
    @CollectionTable(name = "words_length", joinColumns = @JoinColumn(name="fileId"))
    @MapKeyColumn(name = "topWord")
    @Column(name = "count")
    private Map<String, Long> wordLengthMap;

    @Column(nullable = false)
    private Boolean stopWordsExcluded;

}
