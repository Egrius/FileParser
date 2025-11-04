package by.egrius.app.dto;

import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationErrorDto {
    List<ViolationDto> violations = new ArrayList<>();
}
