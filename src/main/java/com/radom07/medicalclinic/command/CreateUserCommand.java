package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserCommand(@NotBlank String firstName,
                                @NotBlank String lastName,
                                @NotBlank @Email String email,
                                @NotBlank String userName,
                                @NotBlank String password) {
}
