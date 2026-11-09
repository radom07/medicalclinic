package com.radom07.medicalclinic.command;

import com.radom07.medicalclinic.model.Specialization;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateDoctorCommand(
        @Schema(example = "1") @NotNull Long userId,
        @Schema(example = "CARDIOLOGIST") @NotNull Specialization specialization,
        @Schema(example = "ABC1234567") @NotBlank String medicalLicenseNo,
        @Schema(example = "[1, 2]") List<Long> facilityIds
        ) {
}