package by.egrius.app.unit.service;

import by.egrius.app.repository.FileEventLogRepository;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.service.FileEventLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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