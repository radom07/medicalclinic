package com.radom07.medicalclinic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BookVisitCommand(
        @Schema(example = "jan@test.pl") @NotBlank @Email String patientEmail
) {
}
