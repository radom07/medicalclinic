package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PatientAlreadyExistsException extends RuntimeException {
  public PatientAlreadyExistsException(String message) {
    super(message);
  }
}
