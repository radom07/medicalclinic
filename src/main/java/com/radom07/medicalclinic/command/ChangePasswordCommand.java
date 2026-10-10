package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordCommand(@NotBlank String password) {
}
