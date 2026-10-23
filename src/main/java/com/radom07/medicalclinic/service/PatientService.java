package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.command.UpdatePatientCommand;
import com.radom07.medicalclinic.exception.PatientAlreadyExistsException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.mapper.PatientMapper;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.entity.Patient;
import com.radom07.medicalclinic.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper mapper;

    public List<PatientDto> getPatients() {
        return patientRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public PatientDto getByEmail(String email) {
        return patientRepository.findByEmail(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
    }

    public PatientDto addPatient(CreatePatientCommand command) {
        if (patientRepository.findByEmail(command.email()).isPresent()) {
            throw new PatientAlreadyExistsException("The patient with the provided email address already exists");
        }
        return mapper.toDto(patientRepository.save(mapper.toEntity(command)));
    }

    @Transactional // bez adnotacji zapytanie na DELETE dostawało 500 Internal Server Error
    public void deleteByEmail(String email) {
        if (!patientRepository.existsByEmail(email)) {
            throw new PatientNotFoundException("The patient with the provided email address does not exist");
        }
        patientRepository.deleteByEmail(email);
    }

    public PatientDto updatePatient(String email, UpdatePatientCommand command) {
        Patient existingPatient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        existingPatient.update(command);
        patientRepository.save(existingPatient);
        /*
        Zadałem sobie pytanie skąd DB będzie wiedziało, że zrobiłem update na wyciągniętym patient i nie będzie wiedziało
        Z tego co sprawdziałem muszę użyć adnotacji @Transactional aby to zabezpieczyć i obsłużyć
        Nie rozmawialiśmy jeszcze o tym więc po prostu do tych dwóch metod dodaje wywołanie metody patientRepository.save()
         */
        return mapper.toDto(existingPatient);
    }

    public PatientDto updatePassword(String email, String password) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        patient.setPassword(password);
        patientRepository.save(patient); // tutaj też można obsłużyć to @Transactional
        return mapper.toDto(patient);
    }
}
