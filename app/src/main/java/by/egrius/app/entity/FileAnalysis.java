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

    private Map<String, Long> startsWithMap;

    private Map<Character, Long> punctuationMap;

    private List<String> topWords;

    private Map<String, Long> wordLengthMap;

    private Boolean stopWordsExcluded;

}
