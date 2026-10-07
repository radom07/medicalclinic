package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.command.UpdatePatientCommand;
import com.radom07.medicalclinic.exception.PatientAlreadyExistsException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.mapper.PatientMapper;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.entity.Patient;
import com.radom07.medicalclinic.repository.InMemoryPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final InMemoryPatientRepository patientRepository;
    private final PatientMapper mapper;

    public List<PatientDto> getPatients() {
        return patientRepository.findAll()
                .stream()
                .map(mapper::entityToDto)
                .toList();
    }

    public PatientDto getByEmail(String email) {
        return patientRepository.findByEmail(email)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
    }

    public PatientDto addPatient(CreatePatientCommand command) {
        if (patientRepository.findByEmail(command.email()).isPresent()) {
            throw new PatientAlreadyExistsException("The patient with the provided email address already exists");
        }
        return mapper.entityToDto(patientRepository.save(mapper.commandToEntity(command)));
    }

    public void deleteByEmail(String email) {
        if (!patientRepository.deleteByEmail(email)) {
            throw new PatientNotFoundException("The patient with the provided email address does not exist");
        }
    }

    public PatientDto updatePatient(String email, UpdatePatientCommand command) {
        Patient existingPatient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        existingPatient.update(command);
        return mapper.entityToDto(existingPatient);
    }

    public PatientDto updatePassword(String email, String password) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        patient.setPassword(password);
        return mapper.entityToDto(patient);
    }
}
