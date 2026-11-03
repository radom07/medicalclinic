package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.AssignDoctorToUserCommand;
import com.radom07.medicalclinic.command.CreateDoctorCommand;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.MedicalFacility;
import com.radom07.medicalclinic.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "medicalFacilities", target = "medicalFacilitiesNames")
    DoctorDto toDto(Doctor entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "medicalFacilities", ignore = true)
    Doctor toEntity(AssignDoctorToUserCommand command, User user);

    default Doctor toEntity(CreateDoctorCommand command, Set<MedicalFacility> facilities) {
        if (command == null)
            return null;
        User user = User.builder()
                .firstName(command.firstName())
                .lastName(command.lastName())
                .email(command.email())
                .userName(command.userName())
                .password(command.password())
                .build();
        return Doctor.builder()
                .specialization(command.specialization())
                .medicalLicenseNo(command.medicalLicenseNo())
                .medicalFacilities(facilities != null ? facilities : new HashSet<>())
                .user(user)
                .build();
    }

    default List<String> mapFacilitiesToNames(Set<MedicalFacility> facilities) {
        if (facilities == null) {
            return Collections.emptyList();
        }
        return facilities.stream()
                .map(MedicalFacility::getName)
                .toList();
    }
}