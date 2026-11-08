package com.radom07.medicalclinic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateFacilityCommand(
        @Schema(example = "Centrum Medyczne Warszawa") @NotBlank String name,
        @Schema(example = "Warszawa") @NotBlank String city,
        @Schema(example = "00-001") @NotBlank String postalCode,
        @Schema(example = "Marszałkowska") @NotBlank String street,
        @Schema(example = "10A") @NotBlank String buildingNo,
        @Schema(example = "221234567") @NotBlank String phoneNumber
) {
}
