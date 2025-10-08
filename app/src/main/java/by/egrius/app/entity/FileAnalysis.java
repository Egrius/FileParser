package by.egrius.app.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Entity
@Table(name="FileAnalysis")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAnalysis {
    @Id
    @OneToOne
    @JoinColumn(name = "fileId", referencedColumnName = "id")
    UploadedFile uploadedFile;

    Map<String, Long> startsWithMap;

    Map<Character, Long> punctuationMap;

    List<String> topWords;

    Map<String, Long> wordLengthMap;

    Boolean stopWordsExcluded;


}
