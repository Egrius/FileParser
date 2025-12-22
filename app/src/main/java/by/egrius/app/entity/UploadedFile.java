    package by.egrius.app.entity;

    import by.egrius.app.entity.enums.ContentType;
    import jakarta.persistence.*;
    import lombok.*;

    import java.sql.Timestamp;
    import java.util.Objects;
    import java.util.UUID;

    @Entity
    @Table(name="UploadedFile")
    @Builder
    @Getter
    @Setter
    @ToString(exclude = {"user", "fileContent", "fileAnalysis", "regexMatch", "fileEventLog"})
    @NoArgsConstructor
    @AllArgsConstructor
    public class UploadedFile {

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "userId", nullable = false)
        private User user;

        @Id
        @GeneratedValue
        @Column(name = "fileId", updatable = false, nullable = false)
        private UUID id;

        @Column(nullable = false, unique = true)
        private String filename;

        @Column(nullable = false)
        private Timestamp uploadTime;

        @Column(nullable = false)
        @Enumerated(value = EnumType.STRING)
        private ContentType contentType;

        @OneToOne(mappedBy = "uploadedFile", fetch = FetchType.LAZY,orphanRemoval = true, cascade = CascadeType.ALL)
        private FileContent fileContent;

        @OneToOne(mappedBy = "uploadedFile", fetch = FetchType.LAZY,orphanRemoval = true, cascade = CascadeType.ALL)
        private FileAnalysis fileAnalysis;

        @OneToOne(mappedBy = "uploadedFile", cascade = CascadeType.ALL, orphanRemoval = true, fetch =FetchType.LAZY)
        private RegexMatch regexMatch;

        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private FileEventLog fileEventLog;

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UploadedFile)) return false;
            UploadedFile that = (UploadedFile) o;
            return Objects.equals(id, that.id);
        }
    }
