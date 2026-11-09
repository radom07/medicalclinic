package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class InvalidVisitTimeException extends MedicalClinicException {
    public InvalidVisitTimeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
