package by.egrius.app.service;

import by.egrius.app.repository.FileEventLogRepository;
import by.egrius.app.repository.UploadedFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileEventLogServiceUnitTest {

    @Mock
    private FileEventLogRepository fileEventLogRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private FileEventLogService fileEventLogService;

    @Test
    void createEventLog_shouldCreateEvent() {

    }
}