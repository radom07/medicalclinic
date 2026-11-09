package com.radom07.medicalclinic.command;

import com.radom07.medicalclinic.model.Specialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignDoctorToUserCommand(
        @NotNull Long userId,
        @NotNull Specialization specialization,
        @NotBlank String medicalLicenseNo
        // Czy tutaj placówki medyczne?
) {
}
