package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateDoctorCommand;
import com.radom07.medicalclinic.exception.DoctorAlreadyAssignedToFacility;
import com.radom07.medicalclinic.exception.DoctorAlreadyExistsException;
import com.radom07.medicalclinic.exception.DoctorNotFoundException;
import com.radom07.medicalclinic.exception.FacilityNotFoundException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.mapper.DoctorMapper;
import com.radom07.medicalclinic.model.Specialization;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.Facility;
import com.radom07.medicalclinic.model.entity.User;
import com.radom07.medicalclinic.repository.DoctorRepository;
import com.radom07.medicalclinic.repository.FacilityRepository;
import com.radom07.medicalclinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DoctorServiceTest {

    private DoctorRepository doctorRepository;
    private UserRepository userRepository;
    private FacilityRepository facilityRepository;
    private DoctorMapper doctorMapper;
    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.userRepository = Mockito.mock(UserRepository.class);
        this.facilityRepository = Mockito.mock(FacilityRepository.class);
        this.doctorMapper = Mappers.getMapper(DoctorMapper.class);
        this.doctorService = new DoctorService(doctorRepository, userRepository, facilityRepository, doctorMapper);
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

    private Facility buildFacility(Long id, String name) {
        return Facility.builder()
                .id(id)
                .name(name)
                .city("Warszawa")
                .postalCode("00-001")
                .street("Marszałkowska")
                .buildingNo("10A")
                .phoneNumber("221234567")
                .build();
    }

    @Test
    void getDoctors_DataCorrect_ReturnDoctorsDto() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Doctor firstDoctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        firstDoctor.addFacility(buildFacility(1L, "Centrum Medyczne Warszawa"));
        Doctor secondDoctor = buildDoctor(2L, "anna@test.pl", "Anna", "Nowak", "annanowak",
                Specialization.SURGEON, "DEF7654321");
        List<Doctor> doctors = List.of(firstDoctor, secondDoctor);
        when(doctorRepository.findAll(pageable)).thenReturn(new PageImpl<>(doctors));
        // when
        List<DoctorDto> doctorsDto = doctorService.getDoctors(pageable).getContent();
        // then
        assertAll(
                () -> assertEquals(2, doctorsDto.size()),
                () -> assertEquals(1L, doctorsDto.get(0).id()),
                () -> assertEquals(1L, doctorsDto.get(0).user().id()),
                () -> assertEquals("jan@test.pl", doctorsDto.get(0).user().email()),
                () -> assertEquals("Jan", doctorsDto.get(0).user().firstName()),
                () -> assertEquals("Kowalski", doctorsDto.get(0).user().lastName()),
                () -> assertEquals("jankowalski", doctorsDto.get(0).user().userName()),
                () -> assertEquals("CARDIOLOGIST", doctorsDto.get(0).specialization()),
                () -> assertEquals(List.of("Centrum Medyczne Warszawa"), doctorsDto.get(0).facilitiesNames()),
                () -> assertEquals(2L, doctorsDto.get(1).id()),
                () -> assertEquals(2L, doctorsDto.get(1).user().id()),
                () -> assertEquals("anna@test.pl", doctorsDto.get(1).user().email()),
                () -> assertEquals("Anna", doctorsDto.get(1).user().firstName()),
                () -> assertEquals("Nowak", doctorsDto.get(1).user().lastName()),
                () -> assertEquals("annanowak", doctorsDto.get(1).user().userName()),
                () -> assertEquals("SURGEON", doctorsDto.get(1).specialization()),
                () -> assertTrue(doctorsDto.get(1).facilitiesNames().isEmpty())
        );
        verify(doctorRepository).findAll(pageable);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void getByEmail_DataCorrect_ReturnDoctorDto() {
        // given
        String email = "jan@test.pl";
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        doctor.addFacility(buildFacility(1L, "Centrum Medyczne Warszawa"));
        when(doctorRepository.findByUserEmailIgnoreCase(email)).thenReturn(Optional.of(doctor));
        // when
        DoctorDto doctorDto = doctorService.getByEmail(email);
        // then
        assertAll(
                () -> assertEquals(1L, doctorDto.id()),
                () -> assertEquals(1L, doctorDto.user().id()),
                () -> assertEquals("jan@test.pl", doctorDto.user().email()),
                () -> assertEquals("Jan", doctorDto.user().firstName()),
                () -> assertEquals("Kowalski", doctorDto.user().lastName()),
                () -> assertEquals("jankowalski", doctorDto.user().userName()),
                () -> assertEquals("CARDIOLOGIST", doctorDto.specialization()),
                () -> assertEquals(List.of("Centrum Medyczne Warszawa"), doctorDto.facilitiesNames())
        );
        verify(doctorRepository).findByUserEmailIgnoreCase(email);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void getByEmail_DoctorNotFound_ThrowsDoctorNotFoundException() {
        // given
        String email = "mail@mail.pl";
        when(doctorRepository.findByUserEmailIgnoreCase(email)).thenReturn(Optional.empty());
        // when
        DoctorNotFoundException notFoundException = assertThrows(DoctorNotFoundException.class, () -> doctorService.getByEmail(email));
        // then
        assertAll(
                () -> assertEquals("The doctor with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(doctorRepository).findByUserEmailIgnoreCase(email);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void addDoctor_DataCorrect_ReturnDoctorDto() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(1L, Specialization.CARDIOLOGIST, "ABC1234567", List.of(1L, 2L));
        User user = buildUser(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski");
        List<Facility> facilities = List.of(
                buildFacility(1L, "Centrum Medyczne Warszawa"),
                buildFacility(2L, "Przychodnia Radom")
        );
        when(doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())).thenReturn(false);
        when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
        when(facilityRepository.findAllById(command.facilityIds())).thenReturn(facilities);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        DoctorDto doctorDto = doctorService.addDoctor(command);
        // then
        assertAll(
                () -> assertEquals(1L, doctorDto.user().id()),
                () -> assertEquals("jan@test.pl", doctorDto.user().email()),
                () -> assertEquals("Jan", doctorDto.user().firstName()),
                () -> assertEquals("Kowalski", doctorDto.user().lastName()),
                () -> assertEquals("jankowalski", doctorDto.user().userName()),
                () -> assertEquals("CARDIOLOGIST", doctorDto.specialization()),
                () -> assertEquals(2, doctorDto.facilitiesNames().size()),
                () -> assertTrue(doctorDto.facilitiesNames().containsAll(List.of("Centrum Medyczne Warszawa", "Przychodnia Radom")))
        );
        verify(doctorRepository).existsByMedicalLicenseNo(command.medicalLicenseNo());
        verify(userRepository).findById(command.userId());
        verify(facilityRepository).findAllById(command.facilityIds());
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void addDoctor_NoFacilityIds_ReturnDoctorDtoWithoutFacilities() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(1L, Specialization.SURGEON, "ABC1234567", null);
        User user = buildUser(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski");
        when(doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())).thenReturn(false);
        when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        DoctorDto doctorDto = doctorService.addDoctor(command);
        // then
        assertAll(
                () -> assertEquals("jan@test.pl", doctorDto.user().email()),
                () -> assertEquals("SURGEON", doctorDto.specialization()),
                () -> assertTrue(doctorDto.facilitiesNames().isEmpty())
        );
        verify(doctorRepository).save(any(Doctor.class));
        verifyNoInteractions(facilityRepository);
    }

    @Test
    void addDoctor_MedicalLicenseNoAlreadyExists_ThrowsDoctorAlreadyExistsException() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(1L, Specialization.CARDIOLOGIST, "ABC1234567", List.of(1L));
        when(doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())).thenReturn(true);
        // when
        DoctorAlreadyExistsException alreadyExistsException = assertThrows(DoctorAlreadyExistsException.class, () -> doctorService.addDoctor(command));
        // then
        assertAll(
                () -> assertEquals("The doctor with the provided medicalLicenseNumber already exists", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, alreadyExistsException.getHttpStatus())
        );
        verifyNoInteractions(userRepository);
        verifyNoInteractions(facilityRepository);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addDoctor_UserNotFound_ThrowsUserNotFoundException() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(99L, Specialization.CARDIOLOGIST, "ABC1234567", List.of(1L));
        when(doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())).thenReturn(false);
        when(userRepository.findById(command.userId())).thenReturn(Optional.empty());
        // when
        UserNotFoundException notFoundException = assertThrows(UserNotFoundException.class, () -> doctorService.addDoctor(command));
        // then
        assertAll(
                () -> assertEquals("The user with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(userRepository).findById(command.userId());
        verifyNoInteractions(facilityRepository);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addDoctor_UserAlreadyDoctor_ThrowsDoctorAlreadyExistsException() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(1L, Specialization.CARDIOLOGIST, "DEF7654321", List.of(1L));
        User user = buildUser(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski");
        Doctor existingDoctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.SURGEON, "ABC1234567");
        user.setDoctor(existingDoctor);
        when(doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())).thenReturn(false);
        when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
        // when
        DoctorAlreadyExistsException alreadyExistsException = assertThrows(DoctorAlreadyExistsException.class, () -> doctorService.addDoctor(command));
        // then
        assertAll(
                () -> assertEquals("This user is already a doctor", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, alreadyExistsException.getHttpStatus())
        );
        verifyNoInteractions(facilityRepository);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addDoctor_FacilityNotFound_ThrowsFacilityNotFoundException() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(1L, Specialization.CARDIOLOGIST, "ABC1234567", List.of(1L, 99L));
        User user = buildUser(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski");
        when(doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())).thenReturn(false);
        when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
        when(facilityRepository.findAllById(command.facilityIds()))
                .thenReturn(List.of(buildFacility(1L, "Centrum Medyczne Warszawa")));
        // when
        FacilityNotFoundException notFoundException = assertThrows(FacilityNotFoundException.class, () -> doctorService.addDoctor(command));
        // then
        assertAll(
                () -> assertEquals("One or more facilities not found", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(facilityRepository).findAllById(command.facilityIds());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void deleteByEmail_DataCorrect_DeletesDoctor() {
        // given
        String email = "jan@test.pl";
        when(doctorRepository.existsByUserEmailIgnoreCase(email)).thenReturn(true);
        // when
        doctorService.deleteByEmail(email);
        // then
        verify(doctorRepository).existsByUserEmailIgnoreCase(email);
        verify(doctorRepository).deleteByUserEmailIgnoreCase(email);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void deleteByEmail_DoctorNotFound_ThrowsDoctorNotFoundException() {
        // given
        String email = "mail@mail.pl";
        when(doctorRepository.existsByUserEmailIgnoreCase(email)).thenReturn(false);
        // when
        DoctorNotFoundException notFoundException = assertThrows(DoctorNotFoundException.class, () -> doctorService.deleteByEmail(email));
        // then
        assertAll(
                () -> assertEquals("The doctor with the provided email address does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(doctorRepository, never()).deleteByUserEmailIgnoreCase(any());
    }

    @Test
    void assignDoctorToFacility_DataCorrect_ReturnDoctorDtoWithFacility() {
        // given
        Long doctorId = 1L;
        Long facilityId = 2L;
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        Facility facility = buildFacility(facilityId, "Przychodnia Radom");
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        // when
        DoctorDto doctorDto = doctorService.assignDoctorToFacility(doctorId, facilityId);
        // then
        assertAll(
                () -> assertEquals(1L, doctorDto.id()),
                () -> assertEquals("jan@test.pl", doctorDto.user().email()),
                () -> assertEquals("CARDIOLOGIST", doctorDto.specialization()),
                () -> assertEquals(List.of("Przychodnia Radom"), doctorDto.facilitiesNames()),
                () -> assertTrue(doctor.getFacilities().contains(facility))
        );
        verify(doctorRepository).findById(doctorId);
        verify(facilityRepository).findById(facilityId);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void assignDoctorToFacility_DoctorNotFound_ThrowsDoctorNotFoundException() {
        // given
        Long doctorId = 99L;
        Long facilityId = 2L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        // when
        DoctorNotFoundException notFoundException = assertThrows(
                DoctorNotFoundException.class, () -> doctorService.assignDoctorToFacility(doctorId, facilityId)
        );
        // then
        assertAll(
                () -> assertEquals("The doctor with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verifyNoInteractions(facilityRepository);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void assignDoctorToFacility_FacilityNotFound_ThrowsFacilityNotFoundException() {
        // given
        Long doctorId = 1L;
        Long facilityId = 99L;
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());
        // when
        FacilityNotFoundException notFoundException = assertThrows(
                FacilityNotFoundException.class, () -> doctorService.assignDoctorToFacility(doctorId, facilityId)
        );
        // then
        assertAll(
                () -> assertEquals("The medical facility with provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus()),
                () -> assertTrue(doctor.getFacilities().isEmpty())
        );
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void assignDoctorToFacility_DoctorAlreadyAssigned_ThrowsDoctorAlreadyAssignedToFacility() {
        // given
        Long doctorId = 1L;
        Long facilityId = 2L;
        Doctor doctor = buildDoctor(1L, "jan@test.pl", "Jan", "Kowalski", "jankowalski",
                Specialization.CARDIOLOGIST, "ABC1234567");
        Facility facility = buildFacility(facilityId, "Przychodnia Radom");
        doctor.addFacility(facility);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        // when
        DoctorAlreadyAssignedToFacility alreadyAssignedException = assertThrows(
                DoctorAlreadyAssignedToFacility.class, () -> doctorService.assignDoctorToFacility(doctorId, facilityId)
        );
        // then
        assertAll(
                () -> assertEquals("The doctor is already assigned to facility", alreadyAssignedException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, alreadyAssignedException.getHttpStatus()),
                () -> assertEquals(1, doctor.getFacilities().size())
        );
        verify(doctorRepository, never()).save(any());
    }
}