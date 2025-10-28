package by.egrius.app.listener;

import by.egrius.app.event.FileEvent;
import by.egrius.app.service.FileEventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileEventListener implements ApplicationListener<FileEvent> {

    private final FileEventLogService fileEventLogService;

    @Override
    public void onApplicationEvent(FileEvent event) {
        fileEventLogService.log(event);
    }
}
