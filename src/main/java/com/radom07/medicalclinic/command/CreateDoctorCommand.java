package com.radom07.medicalclinic.command;

import com.radom07.medicalclinic.model.Specialization;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateDoctorCommand(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String userName,
        @NotNull Specialization specialization,
        @NotBlank String medicalLicenseNo,
        List<Long> facilityIds
        ) {
}
