package com.radom07.medicalclinic.model.dto;

import java.util.List;

public record DoctorDto(
        Long id,
        UserDto user,
        String specialization,
        List<String> facilitiesNames
) {
}