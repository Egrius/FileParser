    package by.egrius.app.entity;

    import by.egrius.app.entity.enums.FileEventType;
    import jakarta.persistence.*;
    import lombok.*;

    import org.hibernate.annotations.GenericGenerator;

    import java.sql.Timestamp;
    import java.util.UUID;

    @Entity
    @Table(name="FileEventLog")
    @Builder
    @ToString(exclude = "uploadedFile")
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class FileEventLog {
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(name = "id", updatable = false, nullable = false)
        private UUID id;

        @Column(name = "file_id")
        private UUID fileId;

        @Enumerated(value = EnumType.STRING)
        private FileEventType fileEventType;

        private Timestamp timestamp;
    }
