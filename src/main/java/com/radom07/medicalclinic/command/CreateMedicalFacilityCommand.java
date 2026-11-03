package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.NotBlank;

public record CreateMedicalFacilityCommand(
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String postalCode,
        @NotBlank String street,
        @NotBlank String buildingNo,
        @NotBlank String phoneNumber
) {
}
