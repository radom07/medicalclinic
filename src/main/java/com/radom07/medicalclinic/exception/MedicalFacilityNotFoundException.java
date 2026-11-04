package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class MedicalFacilityNotFoundException extends MedicalClinicException {
    public MedicalFacilityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
