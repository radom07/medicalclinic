package com.radom07.medicalclinic.model.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
