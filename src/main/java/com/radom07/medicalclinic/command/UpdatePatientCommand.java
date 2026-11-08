package com.radom07.medicalclinic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record UpdatePatientCommand(
        @Schema(example = "nowy@test.pl") @NotBlank @Email String email,
        @Schema(example = "Piotr") @NotBlank String firstName,
        @Schema(example = "Nowak") @NotBlank String lastName,
        @Schema(example = "987654321") @NotBlank String phoneNumber,
        @Schema(example = "piotrnowak") @NotBlank String userName,
        @Schema(example = "1990-05-12") @PastOrPresent @NotNull LocalDate birthday
) {
}