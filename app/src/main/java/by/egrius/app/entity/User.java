package by.egrius.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="Users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    UUID userId;

    @Column(unique = true, nullable = false)
    String username;

    @Email
    @Column(nullable = false, unique = true)
    String email;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<UploadedFile> files;
}