package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreateDoctorCommand;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.Facility;
import com.radom07.medicalclinic.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(source = "facilities", target = "facilitiesNames")
    DoctorDto toDto(Doctor entity);

    @Mapping(target = "id", ignore = true)
    Doctor toEntity(CreateDoctorCommand command, User user, Set<Facility> facilities);

    default List<String> mapFacilitiesToNames(Set<Facility> facilities) {
        if (facilities == null) {
            return Collections.emptyList();
        }
        return facilities.stream()
                .map(Facility::getName)
                .toList();
    }
}