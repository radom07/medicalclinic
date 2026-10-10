package com.radom07.medicalclinic.repository;

import com.radom07.medicalclinic.model.entity.Patient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Repository
public class InMemoryPatientRepository {

    private final List<Patient> patients = new ArrayList<>();

    public List<Patient> findAll() {
        return new ArrayList<>(patients);
    }

    public Optional<Patient> findByEmail(String email) {
        return patients.stream()
                .filter(patient -> nonNull(patient.getEmail()) && patient.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Patient save(Patient patient) {
        patients.add(patient);
        return patient;
    }

    public boolean deleteByEmail(String email) {
        return patients.removeIf(patient -> nonNull(patient.getEmail()) && patient.getEmail().equalsIgnoreCase(email));
    }
}
