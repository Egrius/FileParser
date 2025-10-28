package by.egrius.app.mapper;

import by.egrius.app.dto.fileDTO.RegexMatchReadDto;
import by.egrius.app.entity.RegexMatch;
import by.egrius.app.entity.enums.PatternType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RegexMatchReadMapper implements BaseMapper<RegexMatch, RegexMatchReadDto> {
    @Override
    public RegexMatchReadDto map(RegexMatch object) {
        Map<PatternType, List<String>> map = object.getMatchesByType();

        return new RegexMatchReadDto(
                Optional.ofNullable(map.get(PatternType.EMAIL)).orElse(List.of()),
                Optional.ofNullable(map.get(PatternType.PHONE)).orElse(List.of()),
                Optional.ofNullable(map.get(PatternType.IP)).orElse(List.of()),
                Optional.ofNullable(map.get(PatternType.DATE)).orElse(List.of()),
                object.getTotalMatches()
        );
    }
}
