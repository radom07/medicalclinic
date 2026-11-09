package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.entity.Patient;
import com.radom07.medicalclinic.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientDto toDto(Patient entity);

    @Mapping(target = "id", ignore = true)
    Patient toEntity(CreatePatientCommand command, User user);
}