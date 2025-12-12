package by.egrius.app.mapper;

import by.egrius.app.dto.fileDTO.RegexMatchReadDto;
import by.egrius.app.entity.PatternMatches;
import by.egrius.app.entity.RegexMatch;
import by.egrius.app.entity.enums.PatternType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RegexMatchReadMapper implements BaseMapper<RegexMatch, RegexMatchReadDto> {
    @Override
    public RegexMatchReadDto map(RegexMatch object) {


        Map<PatternType, List<String>> groupedMatches  = object.getPatternMatches().stream()
                .collect(Collectors.groupingBy(
                        PatternMatches::getPatternType,
                        Collectors.mapping(PatternMatches::getMatch, Collectors.toList())
                ));

        return new RegexMatchReadDto(
                groupedMatches.getOrDefault(PatternType.EMAIL, List.of()),
                groupedMatches.getOrDefault(PatternType.PHONE, List.of()),
                groupedMatches.getOrDefault(PatternType.IP, List.of()),
                groupedMatches.getOrDefault(PatternType.DATE, List.of()),
                object.getTotalMatches() != null ? object.getTotalMatches() : 0L
        );
    }
}
