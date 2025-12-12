    package by.egrius.app.entity;

    import by.egrius.app.entity.enums.FileEventType;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.NoArgsConstructor;
    import lombok.ToString;

    import org.hibernate.annotations.GenericGenerator;

    import java.sql.Timestamp;
    import java.util.UUID;

    @Entity
    @Table(name="FileEventLog")
    @Builder
    @ToString(exclude = "uploadedFile")
    @NoArgsConstructor
    @AllArgsConstructor
    public class FileEventLog {
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(name = "id", updatable = false, nullable = false)
        private UUID id;

        @ManyToOne
        @JoinColumn(name = "fileId", nullable = false)
        private UploadedFile uploadedFile;

        @Enumerated(value = EnumType.STRING)
        private FileEventType fileEventType;

        private Timestamp timestamp;
    }
