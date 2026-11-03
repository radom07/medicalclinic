package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.CreateMedicalFacilityCommand;
import com.radom07.medicalclinic.model.dto.MedicalFacilityDto;
import com.radom07.medicalclinic.service.MedicalFacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
@Validated
public class MedicalFacilityController {

    private final MedicalFacilityService medicalFacilityService;

    @GetMapping
    public List<MedicalFacilityDto> getMedicalFacilities() {
        return medicalFacilityService.getFacilities();
    }

    @GetMapping("/{name}")
    public MedicalFacilityDto getMedicalFacilityByName(@PathVariable String name) {
        return medicalFacilityService.getFacilityByName(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalFacilityDto addMedicalFacility(@Valid @RequestBody CreateMedicalFacilityCommand command) {
        return medicalFacilityService.addFacility(command);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicalFacilityByName(@PathVariable String name) {
        medicalFacilityService.deleteFacilityByName(name);
    }
}
