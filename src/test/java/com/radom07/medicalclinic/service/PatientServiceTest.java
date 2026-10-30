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
        this.patientService = new PatientService(patientRepository, patientMapper, userRepository);
    }

    private Patient buildPatient(Long id, String email, String firstName, String lastName, String userName, String idCardNo,
                                 String phoneNumber, LocalDate birthday) {
        User user = User.builder()
                .id(id)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .userName(userName)
                .password("1234")
                .build();
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
                () -> assertEquals("jan@test.pl", patientsDto.get(0).email()),
                () -> assertEquals("Jan", patientsDto.get(0).firstName()),
                () -> assertEquals("Kowalski", patientsDto.get(0).lastName()),
                () -> assertEquals("jankowalski", patientsDto.get(0).userName()),
                () -> assertEquals("111", patientsDto.get(0).phoneNumber()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientsDto.get(0).birthday()),
                () -> assertEquals(2L, patientsDto.get(1).id()),
                () -> assertEquals("anna@test.pl", patientsDto.get(1).email()),
                () -> assertEquals("Anna", patientsDto.get(1).firstName()),
                () -> assertEquals("Nowak", patientsDto.get(1).lastName()),
                () -> assertEquals("annanowak", patientsDto.get(1).userName()),
                () -> assertEquals("222", patientsDto.get(1).phoneNumber()),
                () -> assertEquals(LocalDate.of(2018, 5, 12), patientsDto.get(1).birthday()),
                () -> assertEquals(3L, patientsDto.get(2).id()),
                () -> assertEquals("piotr@test.pl", patientsDto.get(2).email()),
                () -> assertEquals("Piotr", patientsDto.get(2).firstName()),
                () -> assertEquals("Wiśniewski", patientsDto.get(2).lastName()),
                () -> assertEquals("piotrw", patientsDto.get(2).userName()),
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
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("Jan", patientDto.firstName()),
                () -> assertEquals("Kowalski", patientDto.lastName()),
                () -> assertEquals("jankowalski", patientDto.userName()),
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
        CreatePatientCommand command = new CreatePatientCommand("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "jankowalski", "123456789", LocalDate.of(2015, 10, 25));
        when(patientRepository.existsByUserEmailIgnoreCase(command.email())).thenReturn(false);
        when(patientRepository.existsByIdCardNo(command.idCardNo())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        PatientDto patientDto = patientService.addPatient(command);
        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("Jan", patientDto.firstName()),
                () -> assertEquals("Kowalski", patientDto.lastName()),
                () -> assertEquals("jankowalski", patientDto.userName()),
                () -> assertEquals("123456789", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientDto.birthday())
        );
        verify(patientRepository).existsByUserEmailIgnoreCase(command.email());
        verify(patientRepository).existsByIdCardNo(command.idCardNo());
    }

    @Test
    void addPatient_PatientAlreadyExists_ThrowsPatientAlreadyExistsException() {
        // given
        CreatePatientCommand command = new CreatePatientCommand("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "jankowalski", "123456789", LocalDate.of(2015, 10, 25));
        when(patientRepository.existsByUserEmailIgnoreCase("test@test.pl")).thenReturn(true);
        // when
        PatientAlreadyExistsException alreadyExistsException = assertThrows(
                PatientAlreadyExistsException.class, () -> patientService.addPatient(command)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided email address already exists", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, alreadyExistsException.getHttpStatus())
        );
        verify(patientRepository, never()).save(any());
    }

    @Test
    void addPatient_IdCardNoAlreadyExists_ThrowsPatientAlreadyExistsException() {
        // given
        String email = "jan@test.pl";
        CreatePatientCommand command = new CreatePatientCommand("nowy@test.pl", "1234", "0001", "Piotr",
                "Nowak", "piotrnowak", "987654321", LocalDate.of(2025, 1, 10));
        when(patientRepository.existsByUserEmailIgnoreCase(command.email())).thenReturn(false);
        when(patientRepository.existsByIdCardNo(command.idCardNo())).thenReturn(true);
        // when
        PatientAlreadyExistsException alreadyExistsException = assertThrows(
                PatientAlreadyExistsException.class, () -> patientService.addPatient(command)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided IdCardNo already exists", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, alreadyExistsException.getHttpStatus())
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
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("Piotr", patientDto.firstName()),
                () -> assertEquals("Nowak", patientDto.lastName()),
                () -> assertEquals("piotrnowak", patientDto.userName()),
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
                () -> assertEquals("nowy@test.pl", patientDto.email()),
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
        when(patientRepository.findByUserEmailIgnoreCase(email)).thenReturn(Optional.of(patient));
        when(patientRepository.existsByUserEmailIgnoreCase(updatePatientCommand.email())).thenReturn(true);
        // when
        PatientAlreadyExistsException alreadyExistsException = assertThrows(
                PatientAlreadyExistsException.class,
                () -> patientService.updatePatient(email, updatePatientCommand)
        );
        // then
        assertAll(
                () -> assertEquals("You cannot change the email address to one that already exists", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, alreadyExistsException.getHttpStatus()),
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
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("Jan", patientDto.firstName()),
                () -> assertEquals("Kowalski", patientDto.lastName()),
                () -> assertEquals("jankowalski", patientDto.userName()),
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

    @Test
    void createPatientProfile_DataCorrect_ReturnPatientDto() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.pl")
                .firstName("Jan")
                .lastName("Kowalski")
                .userName("jankowalski")
                .password("1234")
                .build();
        CreatePatientProfileCommand command = new CreatePatientProfileCommand("0001", "123456789",
                LocalDate.of(2015, 10, 25));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserEmailIgnoreCase("test@test.pl")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        PatientDto patientDto = patientService.createPatientProfile(userId, command);
        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("Jan", patientDto.firstName()),
                () -> assertEquals("Kowalski", patientDto.lastName()),
                () -> assertEquals("jankowalski", patientDto.userName()),
                () -> assertEquals("123456789", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2015, 10, 25), patientDto.birthday())
        );
        verify(userRepository).findById(userId);
        verify(patientRepository).existsByUserEmailIgnoreCase("test@test.pl");
    }

    @Test
    void createPatientProfile_UserNotFound_ThrowsUserNotFoundException() {
        // given
        Long userId = 99L;
        CreatePatientProfileCommand command = new CreatePatientProfileCommand("0001", "123456789",
                LocalDate.of(2015, 10, 25));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // when
        UserNotFoundException notFoundException = assertThrows(
                UserNotFoundException.class, () -> patientService.createPatientProfile(userId, command)
        );
        // then
        assertAll(
                () -> assertEquals("The user with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(userRepository).findById(userId);
        verifyNoInteractions(patientRepository);
    }

    @Test
    void createPatientProfile_UserAlreadyHasProfile_ThrowsPatientAlreadyExistsException() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.pl")
                .firstName("Jan")
                .lastName("Kowalski")
                .userName("jankowalski")
                .password("1234")
                .build();
        CreatePatientProfileCommand command = new CreatePatientProfileCommand("0001", "123456789",
                LocalDate.of(2015, 10, 25));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserEmailIgnoreCase("test@test.pl")).thenReturn(true);
        // when
        PatientAlreadyExistsException alreadyExistsException = assertThrows(
                PatientAlreadyExistsException.class, () -> patientService.createPatientProfile(userId, command)
        );
        // then
        assertAll(
                () -> assertEquals("This user already has a patient profile", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, alreadyExistsException.getHttpStatus())
        );
        verify(patientRepository, never()).save(any());
    }
}