package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class VisitOverlapException extends MedicalClinicException {
    public VisitOverlapException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
