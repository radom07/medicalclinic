package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BookVisitCommand(
        @NotBlank @Email String patientEmail
) {
}
