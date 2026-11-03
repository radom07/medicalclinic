package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateMedicalFacilityCommand;
import com.radom07.medicalclinic.exception.MedicalFacilityAlreadyExistsException;
import com.radom07.medicalclinic.exception.MedicalFacilityNotFoundException;
import com.radom07.medicalclinic.mapper.MedicalFacilityMapper;
import com.radom07.medicalclinic.model.dto.MedicalFacilityDto;
import com.radom07.medicalclinic.repository.MedicalFacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalFacilityService {

    private final MedicalFacilityRepository medicalFacilityRepository;
    private final MedicalFacilityMapper mapper;

    public List<MedicalFacilityDto> getFacilities() {
        return medicalFacilityRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public MedicalFacilityDto getFacilityByName(String name) {
        return medicalFacilityRepository.findByName(name)
                .map(mapper::toDto)
                .orElseThrow(() -> new MedicalFacilityNotFoundException("The medical facility with the provided name does not exist"));
    }

    public MedicalFacilityDto addFacility(CreateMedicalFacilityCommand command) {
        if (medicalFacilityRepository.existsByName(command.name())) {
            throw new MedicalFacilityAlreadyExistsException("The medical facility with the provided name already exists");
        }
        return mapper.toDto(medicalFacilityRepository.save(mapper.toEntity(command)));
    }

    public void deleteFacilityByName(String name) {
        if (!medicalFacilityRepository.existsByName(name)) {
            throw new MedicalFacilityNotFoundException("The medical facility with the provided name does not exist");
        }
        medicalFacilityRepository.deleteByName(name);
    }
}
