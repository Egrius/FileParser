package by.egrius.app.mapper.fileMapper;

import by.egrius.app.dto.fileDTO.FileReadDto;
import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.mapper.BaseMapper;
import by.egrius.app.mapper.userMapper.UserReadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UploadedFileReadMapper implements BaseMapper<UploadedFile, UploadedFileReadDto> {
    private final UserReadMapper userReadMapper;

    @Override
    public UploadedFileReadDto map(UploadedFile object) {
        return new UploadedFileReadDto(
                object.getId(),
                object.getFilename(),
                object.getUploadTime(),
                object.getContentType()
        );
    }
}
