package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdatePatientCommand(@NotBlank @Email String email,
                                   @NotBlank String firstName,
                                   @NotBlank String lastName,
                                   @NotBlank String phoneNumber,
                                   @NotBlank String userName,
                                   @NotNull LocalDate birthday) {
}
