package by.egrius.app.entity;

import by.egrius.app.entity.enums.FileEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name="FileEventLog")
@ToString(exclude = "uploadedFile")
@NoArgsConstructor
@AllArgsConstructor
public class FileEventLog {
    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "fileId")
    private UploadedFile uploadedFile;

    @Enumerated(value = EnumType.STRING)
    private FileEventType fileEventType;

    private Timestamp timestamp;
}
