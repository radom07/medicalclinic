package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreateFacilityCommand;
import com.radom07.medicalclinic.model.dto.FacilityDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.Facility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface FacilityMapper {

    Facility toEntity(CreateFacilityCommand command);

    @Mapping(source = "doctors", target = "doctorsSpecialization")
    FacilityDto toDto(Facility facility);

    default Set<String> mapDoctorsToSpecializations(Set<Doctor> doctors) {
        if (doctors == null) {
            return Collections.emptySet();
        }
        return doctors.stream()
                .map(doctor -> doctor.getSpecialization().name())
                .collect(Collectors.toSet());
    }
}