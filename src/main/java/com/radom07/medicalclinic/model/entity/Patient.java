package com.radom07.medicalclinic.model.entity;

import com.radom07.medicalclinic.command.UpdatePatientCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class Patient {

    private String email;
    @Setter
    private String password;
    private String idCardNo;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthday;

    public void update(UpdatePatientCommand updatedData) {
        this.email = updatedData.email();
        this.password = updatedData.password();
        this.idCardNo = updatedData.idCardNo();
        this.firstName = updatedData.firstName();
        this.lastName = updatedData.lastName();
        this.phoneNumber = updatedData.phoneNumber();
        this.birthday = updatedData.birthday();
    }
}
