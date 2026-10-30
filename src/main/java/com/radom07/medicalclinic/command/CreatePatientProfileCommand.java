package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePatientProfileCommand(@NotBlank String idCardNo,
                                          @NotBlank String phoneNumber,
                                          @NotNull LocalDate birthday
) {
}
