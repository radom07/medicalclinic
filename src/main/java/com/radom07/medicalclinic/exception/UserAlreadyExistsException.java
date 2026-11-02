package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends MedicalClinicException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
