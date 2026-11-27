package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateFacilityCommand;
import com.radom07.medicalclinic.exception.FacilityAlreadyExistsException;
import com.radom07.medicalclinic.exception.FacilityNotFoundException;
import com.radom07.medicalclinic.mapper.FacilityMapper;
import com.radom07.medicalclinic.model.dto.FacilityDto;
import com.radom07.medicalclinic.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper mapper;

    public Page<FacilityDto> getFacilities(Pageable pageable) {
        return facilityRepository.findAll(pageable)
                .map(mapper::toDto);
    }

    public FacilityDto getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new FacilityNotFoundException("The medical facility with the provided id does not exist"));
    }

    public FacilityDto addFacility(CreateFacilityCommand command) {
        log.info("Creating facility with command={}", command);
        if (facilityRepository.existsByName(command.name())) {
            throw new FacilityAlreadyExistsException("The medical facility with the provided name already exists");
        }
        FacilityDto created = mapper.toDto(facilityRepository.save(mapper.toEntity(command)));
        log.info("Facility created with id={}", created.id());
        return created;
    }

    public void deleteFacilityById(Long id) {
        log.info("Deleting facility id={}", id);
        if (!facilityRepository.existsById(id)) {
            throw new FacilityNotFoundException("The medical facility with the provided id does not exist");
        }
        facilityRepository.deleteById(id);
        log.info("Facility id={} deleted", id);
    }
}