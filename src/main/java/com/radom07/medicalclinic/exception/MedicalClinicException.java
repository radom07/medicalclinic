package com.radom07.medicalclinic.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public abstract class MedicalClinicException extends RuntimeException {

    private final HttpStatus httpStatus;

    public MedicalClinicException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
