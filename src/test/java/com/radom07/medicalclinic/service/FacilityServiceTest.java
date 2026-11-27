package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateFacilityCommand;
import com.radom07.medicalclinic.exception.FacilityAlreadyExistsException;
import com.radom07.medicalclinic.exception.FacilityNotFoundException;
import com.radom07.medicalclinic.mapper.FacilityMapper;
import com.radom07.medicalclinic.model.Specialization;
import com.radom07.medicalclinic.model.dto.FacilityDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.Facility;
import com.radom07.medicalclinic.repository.FacilityRepository;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class FacilityServiceTest {

    private FacilityRepository facilityRepository;
    private FacilityMapper facilityMapper;
    private FacilityService facilityService;

    @BeforeEach
    void setUp() {
        this.facilityRepository = Mockito.mock(FacilityRepository.class);
        this.facilityMapper = Mappers.getMapper(FacilityMapper.class);
        this.facilityService = new FacilityService(facilityRepository, facilityMapper);
    }

    private Doctor buildDoctor(Long id, Specialization specialization) {
        return Doctor.builder()
                .id(id)
                .specialization(specialization)
                .medicalLicenseNo("LICENSE" + id)
                .build();
    }

    private Facility buildFacility(Long id, String name, String city, String postalCode, String street,
                                   String buildingNo, String phoneNumber, Set<Doctor> doctors) {
        return Facility.builder()
                .id(id)
                .name(name)
                .city(city)
                .postalCode(postalCode)
                .street(street)
                .buildingNo(buildingNo)
                .phoneNumber(phoneNumber)
                .doctors(doctors)
                .build();
    }

    @Test
    void getFacilities_DataCorrect_ReturnFacilitiesDto() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Facility> facilities = List.of(
                buildFacility(1L, "Centrum Medyczne Warszawa", "Warszawa", "00-001", "Marszałkowska", "10A",
                        "221234567", Set.of(buildDoctor(1L, Specialization.CARDIOLOGIST))),
                buildFacility(2L, "Przychodnia Radom", "Radom", "26-600", "Żeromskiego", "5",
                        "483334444", Set.of())
        );
        when(facilityRepository.findAll(pageable)).thenReturn(new PageImpl<>(facilities));
        // when
        List<FacilityDto> facilitiesDto = facilityService.getFacilities(pageable).getContent();
        // then
        assertAll(
                () -> assertEquals(2, facilitiesDto.size()),
                () -> assertEquals(1L, facilitiesDto.get(0).id()),
                () -> assertEquals("Centrum Medyczne Warszawa", facilitiesDto.get(0).name()),
                () -> assertEquals("Warszawa", facilitiesDto.get(0).city()),
                () -> assertEquals("00-001", facilitiesDto.get(0).postalCode()),
                () -> assertEquals("Marszałkowska", facilitiesDto.get(0).street()),
                () -> assertEquals("10A", facilitiesDto.get(0).buildingNo()),
                () -> assertEquals("221234567", facilitiesDto.get(0).phoneNumber()),
                () -> assertEquals(Set.of("CARDIOLOGIST"), facilitiesDto.get(0).doctorsSpecialization()),
                () -> assertEquals(2L, facilitiesDto.get(1).id()),
                () -> assertEquals("Przychodnia Radom", facilitiesDto.get(1).name()),
                () -> assertEquals("Radom", facilitiesDto.get(1).city()),
                () -> assertEquals("26-600", facilitiesDto.get(1).postalCode()),
                () -> assertEquals("Żeromskiego", facilitiesDto.get(1).street()),
                () -> assertEquals("5", facilitiesDto.get(1).buildingNo()),
                () -> assertEquals("483334444", facilitiesDto.get(1).phoneNumber()),
                () -> assertEquals(Set.of(), facilitiesDto.get(1).doctorsSpecialization())
        );
        verify(facilityRepository).findAll(pageable);
        verifyNoMoreInteractions(facilityRepository);
    }

    @Test
    void getFacilityById_DataCorrect_ReturnFacilityDto() {
        // given
        Long facilityId = 1L;
        Facility facility = buildFacility(facilityId, "Centrum Medyczne Warszawa", "Warszawa", "00-001",
                "Marszałkowska", "10A", "221234567",
                Set.of(buildDoctor(1L, Specialization.CARDIOLOGIST), buildDoctor(2L, Specialization.SURGEON)));
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        // when
        FacilityDto facilityDto = facilityService.getFacilityById(facilityId);
        // then
        assertAll(
                () -> assertEquals(1L, facilityDto.id()),
                () -> assertEquals("Centrum Medyczne Warszawa", facilityDto.name()),
                () -> assertEquals("Warszawa", facilityDto.city()),
                () -> assertEquals("00-001", facilityDto.postalCode()),
                () -> assertEquals("Marszałkowska", facilityDto.street()),
                () -> assertEquals("10A", facilityDto.buildingNo()),
                () -> assertEquals("221234567", facilityDto.phoneNumber()),
                () -> assertEquals(Set.of("CARDIOLOGIST", "SURGEON"), facilityDto.doctorsSpecialization())
        );
        verify(facilityRepository).findById(facilityId);
        verifyNoMoreInteractions(facilityRepository);
    }

    @Test
    void getFacilityById_FacilityNotFound_ThrowsFacilityNotFoundException() {
        // given
        Long facilityId = 1L;
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());
        // when
        FacilityNotFoundException notFoundException = assertThrows(
                FacilityNotFoundException.class, () -> facilityService.getFacilityById(facilityId)
        );
        // then
        assertAll(
                () -> assertEquals("The medical facility with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(facilityRepository).findById(facilityId);
        verifyNoMoreInteractions(facilityRepository);
    }

    @Test
    void addFacility_DataCorrect_ReturnFacilityDto() {
        // given
        CreateFacilityCommand command = new CreateFacilityCommand("Centrum Medyczne Warszawa", "Warszawa", "00-001",
                "Marszałkowska", "10A", "221234567");
        when(facilityRepository.existsByName(command.name())).thenReturn(false);
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        FacilityDto facilityDto = facilityService.addFacility(command);
        // then
        assertAll(
                () -> assertEquals("Centrum Medyczne Warszawa", facilityDto.name()),
                () -> assertEquals("Warszawa", facilityDto.city()),
                () -> assertEquals("00-001", facilityDto.postalCode()),
                () -> assertEquals("Marszałkowska", facilityDto.street()),
                () -> assertEquals("10A", facilityDto.buildingNo()),
                () -> assertEquals("221234567", facilityDto.phoneNumber())
        );
        verify(facilityRepository).existsByName(command.name());
        verify(facilityRepository).save(any(Facility.class));
        verifyNoMoreInteractions(facilityRepository);
    }

    @Test
    void addFacility_FacilityNameAlreadyExists_ThrowsFacilityAlreadyExistsException() {
        // given
        CreateFacilityCommand command = new CreateFacilityCommand("Centrum Medyczne Warszawa", "Warszawa", "00-001",
                "Marszałkowska", "10A", "221234567");
        when(facilityRepository.existsByName(command.name())).thenReturn(true);
        // when
        FacilityAlreadyExistsException alreadyExistsException = assertThrows(
                FacilityAlreadyExistsException.class, () -> facilityService.addFacility(command)
        );
        // then
        assertAll(
                () -> assertEquals("The medical facility with the provided name already exists", alreadyExistsException.getMessage()),
                () -> assertEquals(HttpStatus.CONFLICT, alreadyExistsException.getHttpStatus())
        );
        verify(facilityRepository).existsByName(command.name());
        verify(facilityRepository, never()).save(any());
    }

    @Test
    void deleteFacilityById_DataCorrect_DeletesFacility() {
        // given
        Long facilityId = 1L;
        when(facilityRepository.existsById(facilityId)).thenReturn(true);
        // when
        facilityService.deleteFacilityById(facilityId);
        // then
        verify(facilityRepository).existsById(facilityId);
        verify(facilityRepository).deleteById(facilityId);
        verifyNoMoreInteractions(facilityRepository);
    }

    @Test
    void deleteFacilityById_FacilityNotFound_ThrowsFacilityNotFoundException() {
        // given
        Long facilityId = 1L;
        when(facilityRepository.existsById(facilityId)).thenReturn(false);
        // when
        FacilityNotFoundException notFoundException = assertThrows(
                FacilityNotFoundException.class, () -> facilityService.deleteFacilityById(facilityId)
        );
        // then
        assertAll(
                () -> assertEquals("The medical facility with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
        verify(facilityRepository, never()).deleteById(any());
    }
}