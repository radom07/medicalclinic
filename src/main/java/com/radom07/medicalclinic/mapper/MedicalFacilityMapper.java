package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreateMedicalFacilityCommand;
import com.radom07.medicalclinic.model.dto.MedicalFacilityDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.MedicalFacility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MedicalFacilityMapper {

    MedicalFacility toEntity(CreateMedicalFacilityCommand command);

    @Mapping(source = "doctors", target = "doctorsSpecialization")
    MedicalFacilityDto toDto(MedicalFacility medicalFacility);

    default Set<String> mapDoctorsToSpecializations(Set<Doctor> doctors) {
        if (doctors == null) {
            return Collections.emptySet();
        }
        return doctors.stream()
                .map(doctor -> doctor.getSpecialization().name())
                .collect(Collectors.toSet());
    }
}