package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateFacilityCommand;
import com.radom07.medicalclinic.exception.FacilityAlreadyExistsException;
import com.radom07.medicalclinic.exception.FacilityNotFoundException;
import com.radom07.medicalclinic.mapper.FacilityMapper;
import com.radom07.medicalclinic.model.dto.FacilityDto;
import com.radom07.medicalclinic.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper mapper;

    public List<FacilityDto> getFacilities() {
        return facilityRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public FacilityDto getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new FacilityNotFoundException("The medical facility with the provided id does not exist"));
    }

    public FacilityDto addFacility(CreateFacilityCommand command) {
        if (facilityRepository.existsByName(command.name())) {
            throw new FacilityAlreadyExistsException("The medical facility with the provided name already exists");
        }
        return mapper.toDto(facilityRepository.save(mapper.toEntity(command)));
    }

    public void deleteFacilityById(Long id) {
        if (!facilityRepository.existsById(id)) {
            throw new FacilityNotFoundException("The medical facility with the provided id does not exist");
        }
        facilityRepository.deleteById(id);
    }
}