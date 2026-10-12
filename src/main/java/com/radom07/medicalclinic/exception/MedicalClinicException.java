package com.radom07.medicalclinic.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
Widzę, że niektórzy taki wyjątek bazowy tworzą abstract
Rozumiem, że na początku rozwoju aplikacji lepiej móc rzucić też takim ogólnym wyjątkiem, a wraz z rozwojem zrobić go abstract
Jakie jest podejście komercyjne?
 */
@Getter
public class MedicalClinicException extends RuntimeException {

    private final HttpStatus httpStatus;

    public MedicalClinicException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
