package com.radom07.medicalclinic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CreatePatientCommand(
        @Schema(example = "jan@test.pl") @NotBlank @Email String email,
        @Schema(example = "haslo123") @NotBlank String password,
        @Schema(example = "ABC1234") @NotBlank String idCardNo,
        @Schema(example = "Jan") @NotBlank String firstName,
        @Schema(example = "Kowalski") @NotBlank String lastName,
        @Schema(example = "jankowalski") @NotBlank String userName,
        @Schema(example = "123456789") @NotBlank String phoneNumber,
        @Schema(example = "2026-11-07") @PastOrPresent @NotNull LocalDate birthday
) {
}