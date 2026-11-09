package com.radom07.medicalclinic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateVisitCommand(
        @Schema(example = "1") @NotNull Long doctorId,
        @Schema(example = "2026-12-01T09:00:00") @NotNull @FutureOrPresent LocalDateTime startTime,
        @Schema(example = "2026-12-01T09:30:00") @NotNull @FutureOrPresent LocalDateTime endTime
) {
}
