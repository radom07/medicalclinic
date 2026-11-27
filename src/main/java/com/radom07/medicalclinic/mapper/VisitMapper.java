package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreateVisitCommand;
import com.radom07.medicalclinic.model.dto.VisitDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.Visit;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {DoctorMapper.class, PatientMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface VisitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", ignore = true)
    Visit toEntity(CreateVisitCommand command, Doctor doctor);

    VisitDto toDto(Visit visit);
}
