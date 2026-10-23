package com.radom07.medicalclinic.model.entity;

import com.radom07.medicalclinic.command.UpdatePatientCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PATIENT")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(name = "ID_CARD", nullable = false, unique = true)
    private String idCardNo;
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;
    @Column
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
