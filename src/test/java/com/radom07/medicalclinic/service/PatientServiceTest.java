package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.command.UpdatePatientCommand;
import com.radom07.medicalclinic.exception.PatientAlreadyExistsException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.mapper.PatientMapper;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.entity.Patient;
import com.radom07.medicalclinic.repository.InMemoryPatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientServiceTest {

    private InMemoryPatientRepository patientRepository;
    private PatientMapper patientMapper;
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        this.patientRepository = Mockito.mock(InMemoryPatientRepository.class);
        this.patientMapper = Mappers.getMapper(PatientMapper.class);
        this.patientService = new PatientService(patientRepository, patientMapper);
    }

    @Test
    void getPatients_DataCorrect_ReturnPatientsDto() {
        // given
        List<Patient> patients = List.of(
                new Patient("jan@test.pl", "1234", "0001", "Jan", "Kowalski",
                        "111", LocalDate.of(2015, 10, 25)),
                new Patient("anna@test.pl", "1234", "0002", "Anna", "Nowak",
                        "222", LocalDate.of(2018, 5, 12)),
                new Patient("piotr@test.pl", "1234", "0003", "Piotr", "Wiśniewski",
                        "333", LocalDate.of(2020, 1, 15))
        );
        when(patientRepository.findAll()).thenReturn(patients);
        // when
        List<PatientDto> patientsDto = patientService.getPatients();
        // then
        assertAll(
                () -> assertEquals(3, patientsDto.size()),
                () -> assertEquals("Jan", patientsDto.get(0).firstName()),
                () -> assertEquals("jan@test.pl", patientsDto.get(0).email()),
                () -> assertEquals("Anna", patientsDto.get(1).firstName()),
                () -> assertEquals("Piotr", patientsDto.get(2).firstName())
        );
    }

    @Test
    void getByEmail_DataCorrect_ReturnPatientDto() {
        // given
        String email = "test@test.pl";
        Optional<Patient> patient = Optional.of(new Patient("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25)));
        when(patientRepository.findByEmail(email)).thenReturn(patient);
        // when
        PatientDto patientDto = patientService.getByEmail(email);
        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("Jan", patientDto.firstName()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientDto.birthday())
        );
    }

    @Test
    void getByEmail_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        String email = "mail@mail.pl";
        when(patientRepository.findByEmail(email)).thenReturn(Optional.empty());
        // when
        PatientNotFoundException notFoundException = assertThrows(
                PatientNotFoundException.class, () -> patientService.getByEmail(email)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
    }

    @Test
    void addPatient_DataCorrect_ReturnPatientDto() {
        // given
        CreatePatientCommand command = new CreatePatientCommand("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25));
        when(patientRepository.findByEmail(command.email())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        PatientDto patientDto = patientService.addPatient(command);
        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("Jan", patientDto.firstName()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientDto.birthday())
        );
    }

    @Test
    void addPatient_PatientAlreadyExists_ThrowsPatientAlreadyExistsException() {
        // given
        CreatePatientCommand command = new CreatePatientCommand("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25));
        Optional<Patient> patient = Optional.of(new Patient("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25)));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(patient);
        // when
        PatientAlreadyExistsException alreadyExistsException = assertThrows(
                PatientAlreadyExistsException.class, () -> patientService.addPatient(command)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address already exists", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, alreadyExistsException.getHttpStatus())
        );
    }

    @Test
    void deleteByEmail_DataCorrect_DeletesPatient() {
        // given
        String email = "test@test.pl";
        when(patientRepository.deleteByEmail(email)).thenReturn(true);
        // when
        patientService.deleteByEmail(email);
        // then
        verify(patientRepository).deleteByEmail(email);
    }

    @Test
    void deleteByEmail_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        String email = "mail@mail.pl";
        when(patientRepository.deleteByEmail(email)).thenReturn(false);
        // when
        PatientNotFoundException notFoundException = assertThrows(
                PatientNotFoundException.class, () -> patientService.deleteByEmail(email)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
    }

    @Test
    void updatePatient_DataCorrect_ReturnUpdatedPatientDto() {
        // given
        String email = "test@test.pl";
        UpdatePatientCommand command = new UpdatePatientCommand("test@test.pl", "1234", "0001", "Piotr",
                "Nowak", "987654321", LocalDate.of(2025, 1, 10));
        Optional<Patient> patient = Optional.of(new Patient("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25)));
        when(patientRepository.findByEmail(email)).thenReturn(patient);
        // when
        PatientDto patientDto = patientService.updatePatient(email, command);
        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("Piotr", patientDto.firstName()),
                () -> assertEquals(LocalDate.of(2025, 1, 10), patientDto.birthday())
        );
    }

    @Test
    void updatePatient_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        String email = "test@test.pl";
        UpdatePatientCommand command = new UpdatePatientCommand("test@test.pl", "1234", "0001", "Piotr",
                "Nowak", "987654321", LocalDate.of(2025, 1, 10));
        when(patientRepository.findByEmail(email)).thenReturn(Optional.empty());
        // when
        PatientNotFoundException notFoundException = assertThrows(
                PatientNotFoundException.class, () -> patientService.updatePatient(email, command)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
    }

    @Test
    void updatePassword_DataCorrect_UpdatesPasswordOnEntity() {
        // given
        String email = "test@test.pl";
        String newPassword = "4321";
        Optional<Patient> patient = Optional.of(new Patient("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25)));
        when(patientRepository.findByEmail(email)).thenReturn(patient);
        // when
        PatientDto patientDto = patientService.updatePassword(email, newPassword);
        // then
        assertEquals("4321", patient.get().getPassword());
    }

    @Test
    void updatePassword_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        String email = "mail@mail.pl";
        String password = "1234";
        when(patientRepository.findByEmail(email)).thenReturn(Optional.empty());
        // when
        PatientNotFoundException notFoundException = assertThrows(
                PatientNotFoundException.class, () -> patientService.updatePassword(email, password)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
    }

}