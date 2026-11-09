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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final DoctorMapper mapper;

    public List<DoctorDto> getDoctors() {
        return doctorRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public DoctorDto getByEmail(String email) {
        return doctorRepository.findByUserEmailIgnoreCase(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new DoctorNotFoundException("The doctor with the provided email address does not exist"));
    }

    public DoctorDto addDoctor(CreateDoctorCommand command) {
        if (doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())) {
            throw new DoctorAlreadyExistsException("The doctor with the provided medicalLicenseNumber already exists");
        }
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("The user with the provided id does not exist"));
        if (user.getDoctor() != null) {
            throw new DoctorAlreadyExistsException("This user is already a doctor");
        }
        Set<Facility> commandFacilities = getValidatedFacilities(command.facilityIds());
        return mapper.toDto(doctorRepository.save(mapper.toEntity(command, user, commandFacilities)));
    }

    @Transactional
    public void deleteByEmail(String email) {
        if (!doctorRepository.existsByUserEmailIgnoreCase(email)) {
            throw new DoctorNotFoundException("The doctor with the provided email address does not exist");
        }
        doctorRepository.deleteByUserEmail(email);
    }

    public DoctorDto assignDoctorToFacility(Long doctorId, Long facilityId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("The doctor with the provided id does not exist"));
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException("The medical facility with provided id does not exist"));
        if (doctor.getFacilities().contains(facility)) {
            throw new DoctorAlreadyAssignedToFacility("The doctor is already assigned to facility");
        }
        doctor.addFacility(facility);
        doctorRepository.save(doctor);
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