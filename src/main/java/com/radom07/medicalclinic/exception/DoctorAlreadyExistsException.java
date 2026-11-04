package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorAlreadyExistsException extends MedicalClinicException {
    public DoctorAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
