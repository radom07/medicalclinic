package com.radom07.medicalclinic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BookVisitCommand(
        @Schema(example = "1") @NotNull Long patientId
) {
}
