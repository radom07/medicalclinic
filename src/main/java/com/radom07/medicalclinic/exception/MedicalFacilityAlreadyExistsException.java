package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class MedicalFacilityAlreadyExistsException extends MedicalClinicException {
    public MedicalFacilityAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
