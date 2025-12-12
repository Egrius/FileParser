package by.egrius.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="Users")
@Builder
@Getter
@Setter
@ToString(exclude = "uploadedFiles")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue
    @Column(name = "userId", updatable = false, nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false)
    @NotEmpty
    private String username;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @NotEmpty
    @Column(nullable = false)
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UploadedFile> uploadedFiles;
}