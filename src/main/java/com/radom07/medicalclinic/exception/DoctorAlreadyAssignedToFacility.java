package com.radom07.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorAlreadyAssignedToFacility extends MedicalClinicException {
    public DoctorAlreadyAssignedToFacility(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
