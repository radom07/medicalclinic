package com.radom07.medicalclinic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Patient {

    private String email;
    @Setter
    private String password;
    private String idCardNo;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthday;

    public void update(Patient updatedData) {
        this.email = updatedData.getEmail();
        this.password = updatedData.getPassword();
        this.idCardNo = updatedData.getIdCardNo();
        this.firstName = updatedData.getFirstName();
        this.lastName = updatedData.getLastName();
        this.phoneNumber = updatedData.getPhoneNumber();
        this.birthday = updatedData.getBirthday();
    }
}
