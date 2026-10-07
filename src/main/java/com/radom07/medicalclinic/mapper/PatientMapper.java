package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public Patient commandToEntity(CreatePatientCommand command) {
        return Patient.builder()
                .email(command.email())
                .password(command.password())
                .idCardNo(command.idCardNo())
                .firstName(command.firstName())
                .lastName(command.lastName())
                .phoneNumber(command.phoneNumber())
                .birthday(command.birthday())
                .build();
    }

    public PatientDto entityToDto(Patient entity) {
        return PatientDto.builder()
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phoneNumber(entity.getPhoneNumber())
                .birthday(entity.getBirthday())
                .build();
    }
}
