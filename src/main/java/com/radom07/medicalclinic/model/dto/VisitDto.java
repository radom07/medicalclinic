package com.radom07.medicalclinic.model.dto;

import java.time.LocalDateTime;

public record VisitDto(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long doctorId,
        String doctorLastName,
        Long patientId
) {
}
