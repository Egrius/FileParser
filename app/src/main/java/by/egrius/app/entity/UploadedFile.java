package by.egrius.app.entity;

import by.egrius.app.entity.enums.ContentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name="UploadedFile")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFile {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    User user;

    @Id
    @Column(name="fileId")
    UUID id;

    @Column(nullable = false)
    String filename;

    @Column(nullable = false)
    Timestamp uploadTime;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    ContentType contentType;

    @OneToOne(mappedBy = "uploadedFile", fetch = FetchType.EAGER)
    FileContent fileContent;

    //Переделать
    @OneToOne(mappedBy = "fileId")
    FileAnalysis fileAnalysis;
}
