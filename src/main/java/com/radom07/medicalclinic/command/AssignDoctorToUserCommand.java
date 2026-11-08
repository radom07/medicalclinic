package com.radom07.medicalclinic.command;

import com.radom07.medicalclinic.model.Specialization;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignDoctorToUserCommand(
        @Schema(example = "1") @NotNull Long userId,
        @Schema(example = "CARDIOLOGIST") @NotNull Specialization specialization,
        @Schema(example = "ABC1234567") @NotBlank String medicalLicenseNo
        // Czy tutaj placówki medyczne?
) {
}
