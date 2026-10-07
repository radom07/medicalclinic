package com.radom07.medicalclinic.model.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PatientDto(String email,
                         String firstName,
                         String lastName,
                         String phoneNumber,
                         LocalDate birthday) {
}
