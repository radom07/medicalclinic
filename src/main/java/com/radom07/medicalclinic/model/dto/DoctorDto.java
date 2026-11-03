package com.radom07.medicalclinic.model.dto;

import java.util.List;

public record DoctorDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String specialization,
        List<String> medicalFacilitiesNames
) {
}