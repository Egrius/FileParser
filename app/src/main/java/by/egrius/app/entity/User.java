package by.egrius.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="Users")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UploadedFile> files;
}