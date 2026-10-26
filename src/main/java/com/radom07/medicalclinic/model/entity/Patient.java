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
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PATIENT")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;
    @Column(name = "PASSWORD", nullable = false)
    private String password;
    @Column(name = "ID_CARD", nullable = false, unique = true)
    @Setter(AccessLevel.PRIVATE)
    private String idCardNo;
    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;
    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;
    @Column(name = "PHONE_NUMBER", nullable = false)
    private String phoneNumber;
    @Column(name = "BIRTHDAY", nullable = false)
    private LocalDate birthday;

    public void update(UpdatePatientCommand updatedData) {
        this.email = Objects.requireNonNull(updatedData.email());
        this.firstName = Objects.requireNonNull(updatedData.firstName());
        this.lastName = Objects.requireNonNull(updatedData.lastName());
        this.phoneNumber = Objects.requireNonNull(updatedData.phoneNumber());
        this.birthday = Objects.requireNonNull(updatedData.birthday());
    }
}
