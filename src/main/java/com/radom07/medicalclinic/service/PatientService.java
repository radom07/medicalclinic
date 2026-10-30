package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.command.CreatePatientProfileCommand;
import com.radom07.medicalclinic.command.UpdatePatientCommand;
import com.radom07.medicalclinic.exception.PatientAlreadyExistsException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.mapper.PatientMapper;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.entity.Patient;
import com.radom07.medicalclinic.model.entity.User;
import com.radom07.medicalclinic.repository.PatientRepository;
import com.radom07.medicalclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper mapper;
    private final UserRepository userRepository;

    public List<PatientDto> getPatients() {
        return patientRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public PatientDto getByEmail(String email) {
        return patientRepository.findByUserEmailIgnoreCase(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
    }

    public PatientDto addPatient(CreatePatientCommand command) {
        if (patientRepository.existsByUserEmailIgnoreCase(command.email())) {
            throw new PatientAlreadyExistsException("The patient with the provided email address already exists");
        }
        if (patientRepository.existsByIdCardNo(command.idCardNo())) {
            throw new PatientAlreadyExistsException("The patient with the provided IdCardNo already exists");
        }
        return mapper.toDto(patientRepository.save(mapper.toEntity(command)));
    }

    @Transactional
    public void deleteByEmail(String email) {
        if (!patientRepository.existsByUserEmailIgnoreCase(email)) {
            throw new PatientNotFoundException("The patient with the provided email address does not exist");
        }
        patientRepository.deleteByUserEmail(email);
    }

    public PatientDto updatePatient(String email, UpdatePatientCommand command) {
        Patient existingPatient = patientRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        if (!email.equalsIgnoreCase(command.email()) && patientRepository.existsByUserEmailIgnoreCase(command.email())) {
            throw new PatientAlreadyExistsException("You cannot change the email address to one that already exists");
        }
        existingPatient.update(command);
        patientRepository.save(existingPatient);
        return mapper.toDto(existingPatient);
    }

    public PatientDto updatePassword(String email, String password) {
        Objects.requireNonNull(password, "password cannot be null");
        Patient patient = patientRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        patient.getUser().setPassword(password);
        patientRepository.save(patient);
        return mapper.toDto(patient);
    }

    public PatientDto createPatientProfile(Long userId, CreatePatientProfileCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("The user with the provided id does not exist"));
        if (patientRepository.existsByUserEmailIgnoreCase(user.getEmail())) {
            throw new PatientAlreadyExistsException("This user already has a patient profile");
        }
        if (patientRepository.existsByIdCardNo(command.idCardNo())) {
            throw new PatientAlreadyExistsException("The patient with the provided IdCardNo already exists");
        }
        return mapper.toDto(patientRepository.save(mapper.toEntity(command, user)));
    }
}
