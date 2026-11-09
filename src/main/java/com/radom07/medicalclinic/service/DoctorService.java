package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.AssignDoctorToFacilityCommand;
import com.radom07.medicalclinic.command.AssignDoctorToUserCommand;
import com.radom07.medicalclinic.command.CreateDoctorCommand;
import com.radom07.medicalclinic.exception.DoctorAlreadyExistsException;
import com.radom07.medicalclinic.exception.DoctorNotFoundException;
import com.radom07.medicalclinic.exception.MedicalFacilityNotFoundException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.mapper.DoctorMapper;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.MedicalFacility;
import com.radom07.medicalclinic.model.entity.User;
import com.radom07.medicalclinic.repository.DoctorRepository;
import com.radom07.medicalclinic.repository.MedicalFacilityRepository;
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
    private final MedicalFacilityRepository medicalFacilityRepository;
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
        if (doctorRepository.existsByUserEmailIgnoreCase(command.email())) {
            throw new DoctorAlreadyExistsException("The doctor with the provided email address already exists");
        }
        if (doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())) {
            throw new DoctorAlreadyExistsException("The doctor with the provided medicalLicenseNumber already exists");
        }
        Set<MedicalFacility> commandFacilities = new HashSet<>();
        if (command.facilityIds() != null && !command.facilityIds().isEmpty()) {
            commandFacilities = new HashSet<>(medicalFacilityRepository.findAllById(command.facilityIds()));
            if (commandFacilities.size() != command.facilityIds().size()) {
                throw new MedicalFacilityNotFoundException("One or more facilities not found");
            }
        }
        return mapper.toDto(doctorRepository.save(mapper.toEntity(command, commandFacilities)));
    }

    @Transactional
    public void deleteByEmail(String email) {
        if (!doctorRepository.existsByUserEmailIgnoreCase(email)) {
            throw new DoctorNotFoundException("The doctor with the provided email address does not exist");
        }
        doctorRepository.deleteByUserEmail(email);
    }

    public DoctorDto assignDoctorToFacility(AssignDoctorToFacilityCommand command) {
        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("The doctor with the provided id does not exist"));
        MedicalFacility medicalFacility = medicalFacilityRepository.findById(command.facilityId())
                .orElseThrow(() -> new MedicalFacilityNotFoundException("The medical facility with provided id does not exist"));
        doctor.addMedicalFacility(medicalFacility);
        doctorRepository.save(doctor);
        return mapper.toDto(doctor);
    }

    public DoctorDto assignDoctorToUser(AssignDoctorToUserCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("The user with the provided id does not exist"));
        if (doctorRepository.existsByUserEmailIgnoreCase(user.getEmail())) {
            throw new DoctorAlreadyExistsException("This user already has a doctor profile");
        }
        if (doctorRepository.existsByMedicalLicenseNo(command.medicalLicenseNo())) {
            throw new DoctorAlreadyExistsException("The doctor with the provided medicalLicenseNumber already exists");
        }
        return mapper.toDto(doctorRepository.save(mapper.toEntity(command, user)));
    }
}
