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

    public Patient getByEmail(String email) {
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist: " + email));
    }

    public Patient addPatient(Patient patient) {
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new PatientAlreadyExistsException("The patient with the provided email address already exists: " + patient.getEmail());
        }
        return patientRepository.save(patient);
    }

    public void deleteByEmail(String email) {
        if (!patientRepository.deleteByEmail(email)) {
            throw new PatientNotFoundException("The patient with the provided email address does not exist: " + email);
        }
    }

    public Patient updatePatient(String email, Patient updatedData) {
        Patient existingPatient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist: " + email));
        existingPatient.update(updatedData);
        return existingPatient;
    }

    public void updatePassword(String email, String password) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist: " + email));
        patient.setPassword(password);
    }
}
