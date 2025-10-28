package by.egrius.app.service;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.mapper.fileMapper.UploadedFileReadMapper;
import by.egrius.app.publisher.FileEventPublisher;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UploadedFileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UploadedFileRepository uploadedFileRepository;
    private final UploadedFileReadMapper uploadedFileReadMapper;
    private final FileEventPublisher fileEventPublisher;

    @Transactional
    public UploadedFileReadDto uploadFile(MultipartFile file, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Не удалось найти пользователя при создании файла"));

        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("unnamed.txt");
        try {
            byte[] fileBytes = file.getBytes();
            String rawText = new String(fileBytes, StandardCharsets.UTF_8);
            long lineCount = rawText.lines().count();
            long wordCount = Arrays.stream(rawText.split("\\s+"))
                    .filter(word -> !word.isBlank())
                    .count();

            UploadedFile uploadedFile = UploadedFile.builder()
                    .id(UUID.randomUUID()) //переделать на генератор
                    .user(user)
                    .filename(filename)
                    .uploadTime(Timestamp.valueOf(LocalDateTime.now()))
                    .contentType(ContentType.TXT)
                    .build();

            FileContent fileContent = FileContent.builder()
                    .uploadedFile(uploadedFile)
                    .rawText(rawText)
                    .lineCount(lineCount)
                    .wordCount(wordCount)
                    .language(null) // Допилить
                    .build();

            uploadedFile.setFileContent(fileContent);

            uploadedFileRepository.save(uploadedFile);

            fileEventPublisher.publishUpload(uploadedFile.getId());

            return uploadedFileReadMapper.map(uploadedFile);
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при чтении содержимого файла", e);
        }
    }

    public UploadedFileReadDto showUploadedFileById(UUID userId, UUID fileId) {
        UploadedFile uploadedFile = uploadedFileRepository.findByIdAndUser_UserId(fileId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Файл не найден или не принадлежит пользователю"));

        return uploadedFileReadMapper.map(uploadedFile);
    }

    public Page<UploadedFileReadDto> showAllUploadedFilesByUserId(UUID userId, Pageable pageable) {
        Page<UploadedFile> files = uploadedFileRepository.findAllByUser_UserId(userId, pageable);
        return files.map(uploadedFileReadMapper::map);
    }

    @Transactional
    public void removeFile(UUID userId, String rawPassword, UUID fileId) throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Не удалось найти пользователя при удалении файла"));

        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("Не удалось найти файл для удаления"));


        if (!uploadedFile.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Файл не принадлежит пользователю");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new AccessDeniedException("Неверный пароль");
        }

        uploadedFileRepository.delete(uploadedFile);

        fileEventPublisher.publishDeleted(fileId);
    }
}