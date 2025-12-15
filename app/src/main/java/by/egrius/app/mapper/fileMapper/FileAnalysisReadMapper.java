package by.egrius.app.mapper.fileMapper;

import by.egrius.app.dto.fileDTO.FileAnalysisReadDto;
import by.egrius.app.entity.FileAnalysis;
import by.egrius.app.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class FileAnalysisReadMapper implements BaseMapper<FileAnalysis, FileAnalysisReadDto> {
    @Override
    public FileAnalysisReadDto map(FileAnalysis object) {
        return new FileAnalysisReadDto(
                object.getId(),
                object.getTopWords(),
                object.getStartsWithMap(),
                object.getPunctuationMap(),
                object.getWordLengthMap(),
                object.getStopWordsExcluded()
        );
    }
}
