package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreatePatientCommand;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper mapper;

    public Page<PatientDto> getPatients(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(mapper::toDto);
    }

    public PatientDto getByEmail(String email) {
        return patientRepository.findByUserEmailIgnoreCase(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
    }

    public PatientDto addPatient(CreatePatientCommand command) {
        log.info("Creating patient with command={}", command);
        if (patientRepository.existsByIdCardNo(command.idCardNo())) {
            throw new PatientAlreadyExistsException("The patient with the provided IdCardNo already exists");
        }
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("The user with the provided id does not exist"));
        if (user.getPatient() != null) {
            throw new PatientAlreadyExistsException("This user is already a patient");
        }
        PatientDto created = mapper.toDto(patientRepository.save(mapper.toEntity(command, user)));
        log.info("Patient created with id={}", created.id());
        return created;
    }

    @Transactional
    public void deleteByEmail(String email) {
        log.info("Deleting patient email={}", email);
        if (!patientRepository.existsByUserEmailIgnoreCase(email)) {
            throw new PatientNotFoundException("The patient with the provided email address does not exist");
        }
        patientRepository.deleteByUserEmailIgnoreCase(email);
        log.info("Patient id={} deleted", email);
    }

    @Transactional
    public PatientDto updatePatient(String email, UpdatePatientCommand command) {
        log.info("Updating patient with email={} and command={}", email, command);
        if (!email.equalsIgnoreCase(command.email()) && patientRepository.existsByUserEmailIgnoreCase(command.email())) {
            throw new PatientAlreadyExistsException("You cannot change the email address to one that already exists");
        }
        Patient existingPatient = patientRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        existingPatient.update(command);
        patientRepository.save(existingPatient);
        log.info("Patient updated");
        return mapper.toDto(existingPatient);
    }

    @Transactional
    public PatientDto updatePassword(String email, String password) {
        log.info("Updating patient's password with email={} and password={}", email, password);
        Objects.requireNonNull(password, "password cannot be null");
        Patient patient = patientRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        patient.getUser().setPassword(password);
        patientRepository.save(patient);
        log.info("Patient's password updated");
        return mapper.toDto(patient);
    }
}