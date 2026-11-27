package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateDoctorCommand;
import com.radom07.medicalclinic.exception.DoctorAlreadyAssignedToFacility;
import com.radom07.medicalclinic.exception.DoctorAlreadyExistsException;
import com.radom07.medicalclinic.exception.DoctorNotFoundException;
import com.radom07.medicalclinic.exception.FacilityNotFoundException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.mapper.DoctorMapper;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.Facility;
import com.radom07.medicalclinic.model.entity.User;
import com.radom07.medicalclinic.repository.DoctorRepository;
import com.radom07.medicalclinic.repository.FacilityRepository;
import com.radom07.medicalclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final DoctorMapper mapper;

    public Page<DoctorDto> getDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable)
                .map(mapper::toDto);
    }

    public DoctorDto getByEmail(String email) {
        return doctorRepository.findByUserEmailIgnoreCase(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new DoctorNotFoundException("The doctor with the provided email address does not exist"));
    }

    public DoctorDto addDoctor(CreateDoctorCommand command) {
        log.info("Creating doctor with command={}", command);
        if (doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())) {
            throw new DoctorAlreadyExistsException("The doctor with the provided medicalLicenseNumber already exists");
        }
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("The user with the provided id does not exist"));
        if (user.getDoctor() != null) {
            throw new DoctorAlreadyExistsException("This user is already a doctor");
        }
        Set<Facility> commandFacilities = getValidatedFacilities(command.facilityIds());
        DoctorDto created = mapper.toDto(doctorRepository.save(mapper.toEntity(command, user, commandFacilities)));
        log.info("Doctor created with id={}", created.id());
        return created;
    }

    @Transactional
    public void deleteByEmail(String email) {
        log.info("Deleting doctor email={}", email);
        if (!doctorRepository.existsByUserEmailIgnoreCase(email)) {
            throw new DoctorNotFoundException("The doctor with the provided email address does not exist");
        }
        doctorRepository.deleteByUserEmailIgnoreCase(email);
        log.info("Doctor email={} deleted", email);
    }

    @Transactional
    public DoctorDto assignDoctorToFacility(Long doctorId, Long facilityId) {
        log.info("Assigning doctor with id={} and facilityId={}", doctorId, facilityId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("The doctor with the provided id does not exist"));
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException("The medical facility with provided id does not exist"));
        if (doctor.getFacilities().contains(facility)) {
            throw new DoctorAlreadyAssignedToFacility("The doctor is already assigned to facility");
        }
        doctor.addFacility(facility);
        doctorRepository.save(doctor);
        log.info("Successfully assigned doctor to facility");
        return mapper.toDto(doctor);
    }

    private Set<Facility> getValidatedFacilities(List<Long> facilityIds) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Facility> facilities = facilityRepository.findAllById(facilityIds);
        if (facilities.size() != facilityIds.size()) {
            throw new FacilityNotFoundException("One or more facilities not found");
        }
        return new HashSet<>(facilities);
    }
}