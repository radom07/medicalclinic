package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class InvalidVisitFilterException extends MedicalClinicException {
    private static final String DEFAULT_MESSAGE = "Cannot filter by patientId and onlyAvailable=true";
    public InvalidVisitFilterException() {
        super(DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
