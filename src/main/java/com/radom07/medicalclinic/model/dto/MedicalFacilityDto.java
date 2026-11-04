package com.radom07.medicalclinic.model.dto;

import java.util.Set;

public record MedicalFacilityDto(
        Long id,
        String name,
        String city,
        String postalCode,
        String street,
        String buildingNo,
        String phoneNumber,
        Set<String> doctorsSpecialization
) {
}