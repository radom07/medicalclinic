package com.radom07.medicalclinic.model.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PatientDto(
        Long id,
        UserDto user,
        String phoneNumber,
        LocalDate birthday
) {
}