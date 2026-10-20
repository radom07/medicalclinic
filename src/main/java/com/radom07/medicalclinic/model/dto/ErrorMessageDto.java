package com.radom07.medicalclinic.model.dto;

import java.time.LocalDateTime;

public record ErrorMessageDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
