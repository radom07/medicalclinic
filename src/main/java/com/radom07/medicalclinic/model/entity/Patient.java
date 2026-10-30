package com.radom07.medicalclinic.model.entity;

import com.radom07.medicalclinic.command.UpdatePatientCommand;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "PATIENTS")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ID_CARD", nullable = false, unique = true)
    @Setter(AccessLevel.PRIVATE)
    private String idCardNo;
    @Column(name = "PHONE_NUMBER", nullable = false)
    private String phoneNumber;
    @Column(name = "BIRTHDAY", nullable = false)
    private LocalDate birthday;
    @OneToOne(cascade = CascadeType.ALL) //  {CascadeType.PERSIST, CascadeType.MERGE} miałem takie, ale DELETE PATIENT = internal server error
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public void update(UpdatePatientCommand updatedData) {
        this.phoneNumber = Objects.requireNonNull(updatedData.phoneNumber());
        this.birthday = Objects.requireNonNull(updatedData.birthday());
        if (this.user != null) {
            this.user.updateUserData(
                    updatedData.firstName(),
                    updatedData.lastName(),
                    updatedData.email(),
                    updatedData.userName()
            );
        }
    }
}