package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.command.CreatePatientProfileCommand;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.entity.Patient;
import com.radom07.medicalclinic.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    default Patient toEntity(CreatePatientCommand command) {
        if (command == null)
            return null;
        User user = User.builder()
                .firstName(command.firstName())
                .lastName(command.lastName())
                .email(command.email())
                .userName(command.userName())
                .password(command.password())
                .build();


        return Patient.builder()
                .idCardNo(command.idCardNo())
                .phoneNumber(command.phoneNumber())
                .birthday(command.birthday())
                .user(user)
                .build();
    }

    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.userName", target = "userName")
    PatientDto toDto(Patient entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    Patient toEntity(CreatePatientProfileCommand command, User user);
}