package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateVisitCommand(
        @NotNull Long doctorId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
}
