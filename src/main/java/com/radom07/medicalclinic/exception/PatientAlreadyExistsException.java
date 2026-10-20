package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class PatientAlreadyExistsException extends MedicalClinicException {
  public PatientAlreadyExistsException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
