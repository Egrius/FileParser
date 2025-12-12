package by.egrius.app.entity;

import by.egrius.app.entity.enums.Language;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.UUID;

@Entity
@Table(name="FileContent")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "uploadedFile")
public class FileContent {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fileId", unique = true, nullable = false)
    private UploadedFile uploadedFile;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    private Long lineCount;

    private Long wordCount;

    @Enumerated(value = EnumType.STRING)
    private Language language;
}
