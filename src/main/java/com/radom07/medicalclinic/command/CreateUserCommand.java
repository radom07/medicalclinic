package com.radom07.medicalclinic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserCommand(
        @Schema(example = "Jan") @NotBlank String firstName,
        @Schema(example = "Kowalski") @NotBlank String lastName,
        @Schema(example = "jan@test.pl") @NotBlank @Email String email,
        @Schema(example = "jankowalski") @NotBlank String userName,
        @Schema(example = "haslo123") @NotBlank String password
) {
}