package by.egrius.app.entity;

import by.egrius.app.entity.enums.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Entity
@Table(name="FileContent")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileContent {
    @Id
    private UUID id;

    // Продумать привязку по айдишнику
    @OneToOne
    @MapsId
    @JoinColumn(name = "fileId")
    UploadedFile uploadedFile;

    String rawText;

    Long lineCount;

    Long wordCount;

    @Enumerated(value = EnumType.STRING)
    Language language;
}
