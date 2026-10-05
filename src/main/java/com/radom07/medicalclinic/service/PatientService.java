package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.exception.PatientAlreadyExistsException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.model.Patient;
import com.radom07.medicalclinic.repository.InMemoryPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final InMemoryPatientRepository patientRepository;

    public List<Patient> getPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist: " + email));
    }

    public Patient addPatient(Patient patient) {
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new PatientAlreadyExistsException("The patient with the provided email address already exists: " + patient.getEmail());
        }
        return patientRepository.save(patient);
    }
    public void deletePatientByEmail(String email) {
        boolean removed = patientRepository.deleteByEmail(email);
        if (!removed) {
            throw new PatientNotFoundException("The patient with the provided email address does not exist: " + email);
        }
    }

    public Patient updatePatient(String email, Patient updatedData) {
        Patient existingPatient = getPatientByEmail(email);
        existingPatient.update(updatedData);
        return existingPatient;
    }
}
