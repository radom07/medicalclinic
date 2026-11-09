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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    private PatientRepository patientRepository;
    private UserRepository userRepository;
    private PatientMapper patientMapper;
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.userRepository = Mockito.mock(UserRepository.class);
        this.patientMapper = Mappers.getMapper(PatientMapper.class);
        this.patientService = new PatientService(patientRepository, userRepository, patientMapper);
    }

    private User buildUser(Long id, String email, String firstName, String lastName, String userName) {
        return User.builder()
                .id(id)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .userName(userName)
                .password("1234")
                .build();
    }

    private Patient buildPatient(Long id, String email, String firstName, String lastName, String userName, String idCardNo,
                                 String phoneNumber, LocalDate birthday) {
        User user = buildUser(id, email, firstName, lastName, userName);
        return Patient.builder()
                .id(id)
                .idCardNo(idCardNo)
                .phoneNumber(phoneNumber)
                .birthday(birthday)
                .user(user)
                .build();
    }

    @Test
    void getPatients_DataCorrect_ReturnPatientsDto() {
        // given
        List<Patient> patients = List.of(
                buildPatient(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                        "0001", "111", LocalDate.of(2015, 10, 25)),
                buildPatient(2L, "anna@test.pl", "Anna", "Nowak", "annanowak",
                        "0002", "222", LocalDate.of(2018, 5, 12)),
                buildPatient(3L, "piotr@test.pl", "Piotr", "Wiśniewski", "piotrw",
                        "0003", "333", LocalDate.of(2020, 1, 15))
        );
        when(patientRepository.findAll()).thenReturn(patients);
        // when
        List<PatientDto> patientsDto = patientService.getPatients();
        // then
        assertAll(
                () -> assertEquals(3, patientsDto.size()),
                () -> assertEquals(1L, patientsDto.get(0).id()),
                () -> assertEquals(1L, patientsDto.get(0).user().id()),
                () -> assertEquals("jan@test.pl", patientsDto.get(0).user().email()),
                () -> assertEquals("Jan", patientsDto.get(0).user().firstName()),
                () -> assertEquals("Kowalski", patientsDto.get(0).user().lastName()),
                () -> assertEquals("jankowalski", patientsDto.get(0).user().userName()),
                () -> assertEquals("111", patientsDto.get(0).phoneNumber()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientsDto.get(0).birthday()),
                () -> assertEquals(2L, patientsDto.get(1).id()),
                () -> assertEquals(2L, patientsDto.get(1).user().id()),
                () -> assertEquals("anna@test.pl", patientsDto.get(1).user().email()),
                () -> assertEquals("Anna", patientsDto.get(1).user().firstName()),
                () -> assertEquals("Nowak", patientsDto.get(1).user().lastName()),
                () -> assertEquals("annanowak", patientsDto.get(1).user().userName()),
                () -> assertEquals("222", patientsDto.get(1).phoneNumber()),
                () -> assertEquals(LocalDate.of(2018, 5, 12), patientsDto.get(1).birthday()),
                () -> assertEquals(3L, patientsDto.get(2).id()),
                () -> assertEquals(3L, patientsDto.get(2).user().id()),
                () -> assertEquals("piotr@test.pl", patientsDto.get(2).user().email()),
                () -> assertEquals("Piotr", patientsDto.get(2).user().firstName()),
                () -> assertEquals("Wiśniewski", patientsDto.get(2).user().lastName()),
                () -> assertEquals("piotrw", patientsDto.get(2).user().userName()),
                () -> assertEquals("333", patientsDto.get(2).phoneNumber()),
                () -> assertEquals(LocalDate.of(2020, 1, 15), patientsDto.get(2).birthday())
        );
        verify(patientRepository).findAll();
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getByEmail_DataCorrect_ReturnPatientDto() {
        // given
        String email = "test@test.pl";
        Optional<Patient> patient = Optional.of(buildPatient(1L, "test@test.pl", "Jan", "Kowalski", "jankowalski",
                "0001", "123456789", LocalDate.of(2015, 10, 25)));
        when(patientRepository.findByUserEmailIgnoreCase(email)).thenReturn(patient);
        // when
        PatientDto patientDto = patientService.getByEmail(email);
        // then
        assertAll(
                () -> assertEquals(1L, patientDto.id()),
                () -> assertEquals(1L, patientDto.user().id()),
                () -> assertEquals("test@test.pl", patientDto.user().email()),
                () -> assertEquals("Jan", patientDto.user().firstName()),
                () -> assertEquals("Kowalski", patientDto.user().lastName()),
                () -> assertEquals("jankowalski", patientDto.user().userName()),
                () -> assertEquals("123456789", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientDto.birthday())
        );
        verify(patientRepository).findByUserEmailIgnoreCase(email);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getByEmail_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        String email = "mail@mail.pl";
        when(patientRepository.findByUserEmailIgnoreCase(email)).thenReturn(Optional.empty());
        // when
        PatientNotFoundException notFoundException = assertThrows(
                PatientNotFoundException.class, () -> patientService.getByEmail(email)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(patientRepository).findByUserEmailIgnoreCase(email);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void addPatient_DataCorrect_ReturnPatientDto() {
        // given
        CreatePatientCommand command = new CreatePatientCommand(1L, "0001", "123456789", LocalDate.of(2015, 10, 25));
        User user = buildUser(1L, "test@test.pl", "Jan", "Kowalski", "jankowalski");
        when(patientRepository.existsByIdCardNo(command.idCardNo())).thenReturn(false);
        when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        PatientDto patientDto = patientService.addPatient(command);
        // then
        assertAll(
                () -> assertEquals(1L, patientDto.user().id()),
                () -> assertEquals("test@test.pl", patientDto.user().email()),
                () -> assertEquals("Jan", patientDto.user().firstName()),
                () -> assertEquals("Kowalski", patientDto.user().lastName()),
                () -> assertEquals("jankowalski", patientDto.user().userName()),
                () -> assertEquals("123456789", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientDto.birthday())
        );
        verify(patientRepository).existsByIdCardNo(command.idCardNo());
        verify(userRepository).findById(command.userId());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void addPatient_IdCardNoAlreadyExists_ThrowsPatientAlreadyExistsException() {
        // given
        CreatePatientCommand command = new CreatePatientCommand(1L, "0001", "123456789", LocalDate.of(2015, 10, 25));
        when(patientRepository.existsByIdCardNo(command.idCardNo())).thenReturn(true);
        // when
        PatientAlreadyExistsException alreadyExistsException = assertThrows(
                PatientAlreadyExistsException.class, () -> patientService.addPatient(command)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided IdCardNo already exists", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, alreadyExistsException.getHttpStatus())
        );
        verifyNoInteractions(userRepository);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void addPatient_UserNotFound_ThrowsUserNotFoundException() {
        // given
        CreatePatientCommand command = new CreatePatientCommand(99L, "0001", "123456789", LocalDate.of(2015, 10, 25));
        when(patientRepository.existsByIdCardNo(command.idCardNo())).thenReturn(false);
        when(userRepository.findById(command.userId())).thenReturn(Optional.empty());
        // when
        UserNotFoundException notFoundException = assertThrows(
                UserNotFoundException.class, () -> patientService.addPatient(command)
        );
        // then
        assertAll(
                () -> assertEquals("The user with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(userRepository).findById(command.userId());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void addPatient_UserAlreadyPatient_ThrowsPatientAlreadyExistsException() {
        // given
        CreatePatientCommand command = new CreatePatientCommand(1L, "0002", "123456789", LocalDate.of(2015, 10, 25));
        User user = buildUser(1L, "test@test.pl", "Jan", "Kowalski", "jankowalski");
        Patient existingPatient = Patient.builder()
                .id(1L)
                .idCardNo("0001")
                .phoneNumber("111")
                .birthday(LocalDate.of(2015, 10, 25))
                .user(user)
                .build();
        user.setPatient(existingPatient);
        when(patientRepository.existsByIdCardNo(command.idCardNo())).thenReturn(false);
        when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
        // when
        PatientAlreadyExistsException alreadyExistsException = assertThrows(
                PatientAlreadyExistsException.class, () -> patientService.addPatient(command)
        );
        // then
        assertAll(
                () -> assertEquals("This user is already a patient", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, alreadyExistsException.getHttpStatus())
        );
        verify(patientRepository, never()).save(any());
    }

    @Test
    void deleteByEmail_DataCorrect_DeletesPatient() {
        // given
        String email = "test@test.pl";
        when(patientRepository.existsByUserEmailIgnoreCase(email)).thenReturn(true);
        // when
        patientService.deleteByEmail(email);
        // then
        verify(patientRepository).existsByUserEmailIgnoreCase(email);
        verify(patientRepository).deleteByUserEmail(email);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void deleteByEmail_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        String email = "mail@mail.pl";
        when(patientRepository.existsByUserEmailIgnoreCase(email)).thenReturn(false);
        // when
        PatientNotFoundException notFoundException = assertThrows(
                PatientNotFoundException.class, () -> patientService.deleteByEmail(email)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(patientRepository, never()).deleteByUserEmail(any());
    }

    @Test
    void updatePatient_DataCorrect_ReturnUpdatedPatientDto() {
        // given
        String email = "test@test.pl";
        UpdatePatientCommand command = new UpdatePatientCommand("test@test.pl", "Piotr", "Nowak",
                "987654321", "piotrnowak", LocalDate.of(2025, 1, 10));
        Optional<Patient> patient = Optional.of(buildPatient(1L, "test@test.pl", "Jan", "Kowalski", "jankowalski",
                "0001", "123456789", LocalDate.of(2015, 10, 25)));
        when(patientRepository.findByUserEmailIgnoreCase(email)).thenReturn(patient);
        // when
        PatientDto patientDto = patientService.updatePatient(email, command);
        // then
        assertAll(
                () -> assertEquals(1L, patientDto.id()),
                () -> assertEquals("test@test.pl", patientDto.user().email()),
                () -> assertEquals("Piotr", patientDto.user().firstName()),
                () -> assertEquals("Nowak", patientDto.user().lastName()),
                () -> assertEquals("piotrnowak", patientDto.user().userName()),
                () -> assertEquals("987654321", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2025, 1, 10), patientDto.birthday()),
                () -> assertEquals("0001", patient.get().getIdCardNo())
        );
        verify(patientRepository).findByUserEmailIgnoreCase(email);
        verify(patientRepository).save(patient.get());
        verify(patientRepository, never()).existsByUserEmailIgnoreCase(any());
    }

    @Test
    void updatePatient_EmailChangedToFreeEmail_ReturnUpdatedPatientDto() {
        // given
        String email = "test@test.pl";
        UpdatePatientCommand command = new UpdatePatientCommand("nowy@test.pl", "Piotr", "Nowak",
                "987654321", "piotrnowak", LocalDate.of(2025, 1, 10));
        Patient patient = buildPatient(1L, "test@test.pl", "Jan", "Kowalski", "jankowalski",
                "0001", "123456789", LocalDate.of(2015, 10, 25));
        when(patientRepository.findByUserEmailIgnoreCase(email)).thenReturn(Optional.of(patient));
        when(patientRepository.existsByUserEmailIgnoreCase("nowy@test.pl")).thenReturn(false);
        // when
        PatientDto patientDto = patientService.updatePatient(email, command);
        // then
        assertAll(
                () -> assertEquals("nowy@test.pl", patientDto.user().email()),
                () -> assertEquals("nowy@test.pl", patient.getUser().getEmail())
        );
        verify(patientRepository).existsByUserEmailIgnoreCase("nowy@test.pl");
        verify(patientRepository).save(patient);
    }

    @Test
    void updatePatient_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        String email = "test@test.pl";
        UpdatePatientCommand command = new UpdatePatientCommand("test@test.pl", "Piotr", "Nowak",
                "987654321", "piotrnowak", LocalDate.of(2025, 1, 10));
        when(patientRepository.findByUserEmailIgnoreCase(email)).thenReturn(Optional.empty());
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
    void updatePatient_EmailTakenByAnotherPatient_ThrowsPatientAlreadyExistsException() {
        // given
        String email = "jan@test.pl";
        UpdatePatientCommand updatePatientCommand = new UpdatePatientCommand("zajety@test.pl", "Piotr", "Nowak",
                "987654321", "piotrnowak", LocalDate.of(2025, 1, 10));
        Patient patient = buildPatient(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                "0001", "111", LocalDate.of(2015, 10, 25));
        when(patientRepository.existsByUserEmailIgnoreCase(updatePatientCommand.email())).thenReturn(true);
        // when
        PatientAlreadyExistsException alreadyExistsException = assertThrows(
                PatientAlreadyExistsException.class,
                () -> patientService.updatePatient(email, updatePatientCommand)
        );
        // then
        assertAll(
                () -> assertEquals("You cannot change the email address to one that already exists", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, alreadyExistsException.getHttpStatus()),
                () -> assertEquals("jan@test.pl", patient.getUser().getEmail()),
                () -> assertEquals("Jan", patient.getUser().getFirstName())
        );
        verify(patientRepository, never()).save(any());
    }

    @Test
    void updatePassword_DataCorrect_UpdatesPasswordOnEntity() {
        // given
        String email = "test@test.pl";
        String newPassword = "4321";
        Optional<Patient> patient = Optional.of(buildPatient(1L, "test@test.pl", "Jan", "Kowalski", "jankowalski",
                "0001", "123456789", LocalDate.of(2015, 10, 25)));
        when(patientRepository.findByUserEmailIgnoreCase(email)).thenReturn(patient);
        // when
        PatientDto patientDto = patientService.updatePassword(email, newPassword);
        // then
        assertAll(
                () -> assertEquals("4321", patient.get().getUser().getPassword()),
                () -> assertEquals("test@test.pl", patientDto.user().email()),
                () -> assertEquals("Jan", patientDto.user().firstName()),
                () -> assertEquals("Kowalski", patientDto.user().lastName()),
                () -> assertEquals("jankowalski", patientDto.user().userName()),
                () -> assertEquals("123456789", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientDto.birthday())
        );
        verify(patientRepository).findByUserEmailIgnoreCase(email);
        verify(patientRepository).save(patient.get());
    }

    @Test
    void updatePassword_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        String email = "mail@mail.pl";
        String password = "1234";
        when(patientRepository.findByUserEmailIgnoreCase(email)).thenReturn(Optional.empty());
        // when
        PatientNotFoundException notFoundException = assertThrows(
                PatientNotFoundException.class, () -> patientService.updatePassword(email, password)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(patientRepository, never()).save(any());
    }
}