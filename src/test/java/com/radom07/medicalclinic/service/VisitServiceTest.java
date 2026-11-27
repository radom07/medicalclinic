package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateVisitCommand;
import com.radom07.medicalclinic.exception.DoctorNotFoundException;
import com.radom07.medicalclinic.exception.InvalidVisitFilterException;
import com.radom07.medicalclinic.exception.InvalidVisitTimeException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.exception.VisitAlreadyBookedException;
import com.radom07.medicalclinic.exception.VisitNotFoundException;
import com.radom07.medicalclinic.exception.VisitOverlapException;
import com.radom07.medicalclinic.mapper.DoctorMapper;
import com.radom07.medicalclinic.mapper.PatientMapper;
import com.radom07.medicalclinic.mapper.VisitMapper;
import com.radom07.medicalclinic.mapper.VisitMapperImpl;
import com.radom07.medicalclinic.model.Specialization;
import com.radom07.medicalclinic.model.dto.VisitDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.Patient;
import com.radom07.medicalclinic.model.entity.User;
import com.radom07.medicalclinic.model.entity.Visit;
import com.radom07.medicalclinic.repository.DoctorRepository;
import com.radom07.medicalclinic.repository.PatientRepository;
import com.radom07.medicalclinic.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VisitServiceTest {

    private static final LocalDateTime FUTURE_START = LocalDateTime.of(2099, 12, 1, 9, 0);
    private static final LocalDateTime FUTURE_END = LocalDateTime.of(2099, 12, 1, 9, 30);
    private static final LocalDateTime PAST_START = LocalDateTime.of(2000, 1, 1, 9, 0);
    private static final LocalDateTime PAST_END = LocalDateTime.of(2000, 1, 1, 9, 30);

    private VisitRepository visitRepository;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;
    private VisitMapper visitMapper;
    private VisitService visitService;

    @BeforeEach
    void setUp() {
        this.visitRepository = Mockito.mock(VisitRepository.class);
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.visitMapper = new VisitMapperImpl(
                Mappers.getMapper(DoctorMapper.class),
                Mappers.getMapper(PatientMapper.class)
        );
        this.visitService = new VisitService(visitRepository, doctorRepository, patientRepository, visitMapper);
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

    private Doctor buildDoctor(Long id, String email, String firstName, String lastName, String userName,
                               Specialization specialization, String medicalLicenseNo) {
        User user = buildUser(id, email, firstName, lastName, userName);
        return Doctor.builder()
                .id(id)
                .specialization(specialization)
                .medicalLicenseNo(medicalLicenseNo)
                .user(user)
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

    private Visit buildVisit(Long id, LocalDateTime startTime, LocalDateTime endTime, Doctor doctor, Patient patient) {
        return Visit.builder()
                .id(id)
                .startTime(startTime)
                .endTime(endTime)
                .doctor(doctor)
                .patient(patient)
                .build();
    }

    @Test
    void getVisitsByCriteria_NoFilters_ReturnAllVisitsDto() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        Patient patient = buildPatient(1L, "anna@test.pl", "Anna", "Nowak", "annanowak",
                "0001", "123456789", LocalDate.of(1990, 5, 12));
        List<Visit> visits = List.of(
                buildVisit(1L, FUTURE_START, FUTURE_END, doctor, patient),
                buildVisit(2L, FUTURE_START.plusMinutes(30), FUTURE_END.plusMinutes(30), doctor, null)
        );
        when(visitRepository.findAll(pageable)).thenReturn(new PageImpl<>(visits));
        // when
        List<VisitDto> visitsDto = visitService.getVisitsByCriteria(null, false, pageable).getContent();
        // then
        assertAll(
                () -> assertEquals(2, visitsDto.size()),
                () -> assertEquals(1L, visitsDto.get(0).id()),
                () -> assertEquals(FUTURE_START, visitsDto.get(0).startTime()),
                () -> assertEquals(FUTURE_END, visitsDto.get(0).endTime()),
                () -> assertEquals(1L, visitsDto.get(0).doctor().id()),
                () -> assertEquals("jan@test.pl", visitsDto.get(0).doctor().user().email()),
                () -> assertEquals("CARDIOLOGIST", visitsDto.get(0).doctor().specialization()),
                () -> assertEquals(1L, visitsDto.get(0).patient().id()),
                () -> assertEquals("anna@test.pl", visitsDto.get(0).patient().user().email()),
                () -> assertEquals("123456789", visitsDto.get(0).patient().phoneNumber()),
                () -> assertEquals(2L, visitsDto.get(1).id()),
                () -> assertEquals(FUTURE_START.plusMinutes(30), visitsDto.get(1).startTime()),
                () -> assertEquals(FUTURE_END.plusMinutes(30), visitsDto.get(1).endTime()),
                () -> assertEquals(1L, visitsDto.get(1).doctor().id()),
                () -> assertNull(visitsDto.get(1).patient())
        );
        verify(visitRepository).findAll(pageable);
        verifyNoMoreInteractions(visitRepository);
    }

    @Test
    void getVisitsByCriteria_PatientId_ReturnPatientVisitsDto() {
        // given
        Long patientId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        Patient patient = buildPatient(patientId, "anna@test.pl", "Anna", "Nowak", "annanowak",
                "0001", "123456789", LocalDate.of(1990, 5, 12));
        List<Visit> visits = List.of(buildVisit(1L, FUTURE_START, FUTURE_END, doctor, patient));
        when(visitRepository.findByPatientId(patientId, pageable)).thenReturn(new PageImpl<>(visits));
        // when
        List<VisitDto> visitsDto = visitService.getVisitsByCriteria(patientId, false, pageable).getContent();
        // then
        assertAll(
                () -> assertEquals(1, visitsDto.size()),
                () -> assertEquals(1L, visitsDto.get(0).id()),
                () -> assertEquals(FUTURE_START, visitsDto.get(0).startTime()),
                () -> assertEquals(FUTURE_END, visitsDto.get(0).endTime()),
                () -> assertEquals(1L, visitsDto.get(0).doctor().id()),
                () -> assertEquals(patientId, visitsDto.get(0).patient().id()),
                () -> assertEquals("anna@test.pl", visitsDto.get(0).patient().user().email())
        );
        verify(visitRepository).findByPatientId(patientId, pageable);
        verifyNoMoreInteractions(visitRepository);
    }

    @Test
    void getVisitsByCriteria_OnlyAvailable_ReturnAvailableVisitsDto() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        List<Visit> visits = List.of(buildVisit(2L, FUTURE_START, FUTURE_END, doctor, null));
        when(visitRepository.findByPatientIsNullAndStartTimeAfter(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(new PageImpl<>(visits));
        // when
        List<VisitDto> visitsDto = visitService.getVisitsByCriteria(null, true, pageable).getContent();
        // then
        assertAll(
                () -> assertEquals(1, visitsDto.size()),
                () -> assertEquals(2L, visitsDto.get(0).id()),
                () -> assertEquals(FUTURE_START, visitsDto.get(0).startTime()),
                () -> assertEquals(FUTURE_END, visitsDto.get(0).endTime()),
                () -> assertEquals(1L, visitsDto.get(0).doctor().id()),
                () -> assertNull(visitsDto.get(0).patient())
        );
        verify(visitRepository).findByPatientIsNullAndStartTimeAfter(any(LocalDateTime.class), eq(pageable));
        verifyNoMoreInteractions(visitRepository);
    }

    @Test
    void getVisitsByCriteria_PatientIdAndOnlyAvailable_ThrowsInvalidVisitFilterException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        // when
        InvalidVisitFilterException invalidFilterException = assertThrows(
                InvalidVisitFilterException.class, () -> visitService.getVisitsByCriteria(1L, true, pageable)
        );
        // then
        assertAll(
                () -> assertEquals("Cannot filter by patientId and onlyAvailable=true", invalidFilterException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, invalidFilterException.getHttpStatus())
        );
        verifyNoInteractions(visitRepository);
    }

    @Test
    void addVisit_DataCorrect_ReturnVisitDto() {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, FUTURE_START, FUTURE_END);
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        when(doctorRepository.findById(command.doctorId())).thenReturn(Optional.of(doctor));
        when(visitRepository.countOverlappingVisits(doctor.getId(), command.startTime(), command.endTime())).thenReturn(0L);
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        VisitDto visitDto = visitService.addVisit(command);
        // then
        assertAll(
                () -> assertEquals(FUTURE_START, visitDto.startTime()),
                () -> assertEquals(FUTURE_END, visitDto.endTime()),
                () -> assertEquals(1L, visitDto.doctor().id()),
                () -> assertEquals("jan@test.pl", visitDto.doctor().user().email()),
                () -> assertEquals("CARDIOLOGIST", visitDto.doctor().specialization()),
                () -> assertNull(visitDto.patient())
        );
        verify(doctorRepository).findById(command.doctorId());
        verify(visitRepository).countOverlappingVisits(doctor.getId(), command.startTime(), command.endTime());
        verify(visitRepository).save(any(Visit.class));
    }

    @Test
    void addVisit_StartTimeInPast_ThrowsInvalidVisitTimeException() {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, PAST_START, PAST_END);
        // when
        InvalidVisitTimeException invalidTimeException = assertThrows(
                InvalidVisitTimeException.class, () -> visitService.addVisit(command)
        );
        // then
        assertAll(
                () -> assertEquals("Cannot create or book a visit in the past", invalidTimeException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, invalidTimeException.getHttpStatus())
        );
        verifyNoInteractions(doctorRepository);
        verifyNoInteractions(visitRepository);
    }

    @Test
    void addVisit_StartTimeNotQuarterHour_ThrowsInvalidVisitTimeException() {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, FUTURE_START.plusMinutes(10), FUTURE_END);
        // when
        InvalidVisitTimeException invalidTimeException = assertThrows(
                InvalidVisitTimeException.class, () -> visitService.addVisit(command)
        );
        // then
        assertAll(
                () -> assertEquals("Time must be in full quarter-hour", invalidTimeException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, invalidTimeException.getHttpStatus())
        );
        verifyNoInteractions(doctorRepository);
        verifyNoInteractions(visitRepository);
    }

    @Test
    void addVisit_EndTimeNotQuarterHour_ThrowsInvalidVisitTimeException() {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, FUTURE_START, FUTURE_END.plusMinutes(10));
        // when
        InvalidVisitTimeException invalidTimeException = assertThrows(
                InvalidVisitTimeException.class, () -> visitService.addVisit(command)
        );
        // then
        assertAll(
                () -> assertEquals("Time must be in full quarter-hour", invalidTimeException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, invalidTimeException.getHttpStatus())
        );
        verifyNoInteractions(doctorRepository);
        verifyNoInteractions(visitRepository);
    }

    @Test
    void addVisit_EndTimeBeforeStartTime_ThrowsInvalidVisitTimeException() {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, FUTURE_END, FUTURE_START);
        // when
        InvalidVisitTimeException invalidTimeException = assertThrows(
                InvalidVisitTimeException.class, () -> visitService.addVisit(command)
        );
        // then
        assertAll(
                () -> assertEquals("The end time must be after start time", invalidTimeException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, invalidTimeException.getHttpStatus())
        );
        verifyNoInteractions(doctorRepository);
        verifyNoInteractions(visitRepository);
    }

    @Test
    void addVisit_DoctorNotFound_ThrowsDoctorNotFoundException() {
        // given
        CreateVisitCommand command = new CreateVisitCommand(99L, FUTURE_START, FUTURE_END);
        when(doctorRepository.findById(command.doctorId())).thenReturn(Optional.empty());
        // when
        DoctorNotFoundException notFoundException = assertThrows(
                DoctorNotFoundException.class, () -> visitService.addVisit(command)
        );
        // then
        assertAll(
                () -> assertEquals("The doctor with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(doctorRepository).findById(command.doctorId());
        verifyNoInteractions(visitRepository);
    }

    @Test
    void addVisit_VisitOverlap_ThrowsVisitOverlapException() {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, FUTURE_START, FUTURE_END);
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        when(doctorRepository.findById(command.doctorId())).thenReturn(Optional.of(doctor));
        when(visitRepository.countOverlappingVisits(doctor.getId(), command.startTime(), command.endTime())).thenReturn(1L);
        // when
        VisitOverlapException overlapException = assertThrows(
                VisitOverlapException.class, () -> visitService.addVisit(command)
        );
        // then
        assertAll(
                () -> assertEquals("The doctor already has a scheduled visit", overlapException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, overlapException.getHttpStatus())
        );
        verify(visitRepository).countOverlappingVisits(doctor.getId(), command.startTime(), command.endTime());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookVisit_DataCorrect_ReturnBookedVisitDto() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        Patient patient = buildPatient(patientId, "anna@test.pl", "Anna", "Nowak", "annanowak",
                "0001", "123456789", LocalDate.of(1990, 5, 12));
        Visit visit = buildVisit(visitId, FUTURE_START, FUTURE_END, doctor, null);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        VisitDto visitDto = visitService.bookVisit(visitId, patientId);
        // then
        assertAll(
                () -> assertEquals(1L, visitDto.id()),
                () -> assertEquals(FUTURE_START, visitDto.startTime()),
                () -> assertEquals(FUTURE_END, visitDto.endTime()),
                () -> assertEquals(1L, visitDto.doctor().id()),
                () -> assertEquals(patientId, visitDto.patient().id()),
                () -> assertEquals("anna@test.pl", visitDto.patient().user().email()),
                () -> assertEquals("Anna", visitDto.patient().user().firstName()),
                () -> assertEquals("Nowak", visitDto.patient().user().lastName()),
                () -> assertEquals("123456789", visitDto.patient().phoneNumber()),
                () -> assertEquals(LocalDate.of(1990, 5, 12), visitDto.patient().birthday()),
                () -> assertEquals(patient, visit.getPatient())
        );
        verify(visitRepository).findById(visitId);
        verify(patientRepository).findById(patientId);
        verify(visitRepository).save(visit);
    }

    @Test
    void bookVisit_VisitNotFound_ThrowsVisitNotFoundException() {
        // given
        Long visitId = 99L;
        Long patientId = 1L;
        when(visitRepository.findById(visitId)).thenReturn(Optional.empty());
        // when
        VisitNotFoundException notFoundException = assertThrows(
                VisitNotFoundException.class, () -> visitService.bookVisit(visitId, patientId)
        );
        // then
        assertAll(
                () -> assertEquals("The visit with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verifyNoInteractions(patientRepository);
        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookVisit_VisitInPast_ThrowsInvalidVisitTimeException() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        Visit visit = buildVisit(visitId, PAST_START, PAST_END, doctor, null);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        // when
        InvalidVisitTimeException invalidTimeException = assertThrows(
                InvalidVisitTimeException.class, () -> visitService.bookVisit(visitId, patientId)
        );
        // then
        assertAll(
                () -> assertEquals("Cannot create or book a visit in the past", invalidTimeException.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, invalidTimeException.getHttpStatus()),
                () -> assertNull(visit.getPatient())
        );
        verifyNoInteractions(patientRepository);
        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookVisit_VisitAlreadyBooked_ThrowsVisitAlreadyBookedException() {
        // given
        Long visitId = 1L;
        Long patientId = 2L;
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        Patient bookedPatient = buildPatient(1L, "anna@test.pl", "Anna", "Nowak", "annanowak",
                "0001", "123456789", LocalDate.of(1990, 5, 12));
        Visit visit = buildVisit(visitId, FUTURE_START, FUTURE_END, doctor, bookedPatient);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        // when
        VisitAlreadyBookedException alreadyBookedException = assertThrows(
                VisitAlreadyBookedException.class, () -> visitService.bookVisit(visitId, patientId)
        );
        // then
        assertAll(
                () -> assertEquals("This visit is already booked", alreadyBookedException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, alreadyBookedException.getHttpStatus()),
                () -> assertEquals(bookedPatient, visit.getPatient())
        );
        verifyNoInteractions(patientRepository);
        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookVisit_PatientNotFound_ThrowsPatientNotFoundException() {
        // given
        Long visitId = 1L;
        Long patientId = 99L;
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        Visit visit = buildVisit(visitId, FUTURE_START, FUTURE_END, doctor, null);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        // when
        PatientNotFoundException notFoundException = assertThrows(
                PatientNotFoundException.class, () -> visitService.bookVisit(visitId, patientId)
        );
        // then
        assertAll(
                () -> assertEquals("The patient with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus()),
                () -> assertNull(visit.getPatient())
        );
        verify(patientRepository).findById(patientId);
        verify(visitRepository, never()).save(any());
    }
}