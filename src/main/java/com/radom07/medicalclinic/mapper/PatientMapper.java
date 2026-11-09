package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    @Mapping(target = "id", ignore = true)
    Patient toEntity(CreatePatientCommand command);
    PatientDto toDto(Patient entity);
}