package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePatientCommand(@NotBlank @Email String email,
                                   @NotBlank String password,
                                   @NotBlank String idCardNo,
                                   @NotBlank String firstName,
                                   @NotBlank String lastName,
                                   @NotBlank String phoneNumber,
                                   @NotNull LocalDate birthday) {
}
