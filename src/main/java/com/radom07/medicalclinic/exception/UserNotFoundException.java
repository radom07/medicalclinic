package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends MedicalClinicException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
