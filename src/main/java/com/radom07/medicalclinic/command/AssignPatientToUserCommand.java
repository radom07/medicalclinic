package com.radom07.medicalclinic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record AssignPatientToUserCommand(
        @Schema(example = "1") @NotNull Long userId,
        @Schema(example = "ABC1234") @NotBlank String idCardNo,
        @Schema(example = "123456789") @NotBlank String phoneNumber,
        @Schema(example = "2026-11-07") @PastOrPresent @NotNull LocalDate birthday
) {
}