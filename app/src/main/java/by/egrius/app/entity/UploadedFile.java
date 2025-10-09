package by.egrius.app.entity;

import by.egrius.app.entity.enums.ContentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name="UploadedFile")
@Builder
@ToString(exclude = {"user", "fileContent", "fileAnalysis", "regexMatch", "fileEventLog"})
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFile {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Id
    @Column(name="fileId")
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private Timestamp uploadTime;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ContentType contentType;

    @OneToOne(mappedBy = "uploadedFile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private FileContent fileContent;

    @OneToOne(mappedBy = "uploadedFile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private FileAnalysis fileAnalysis;

    @OneToOne(mappedBy = "uploadedFile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RegexMatch regexMatch;

    @OneToOne(mappedBy = "uploadedFile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FileEventLog fileEventLog;

}
