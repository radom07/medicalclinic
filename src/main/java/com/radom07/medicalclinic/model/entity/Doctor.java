package com.radom07.medicalclinic.model.entity;

import com.radom07.medicalclinic.model.Specialization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "DOCTORS")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "SPECIALIZATION", nullable = false)
    @Enumerated(EnumType.STRING)
    private Specialization specialization;
    @Column(name = "MEDICAL_LICENSE", nullable = false, unique = true)
    private String medicalLicenseNo;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "DOCTOR_FACILITIES",
            joinColumns = @JoinColumn(name = "DOCTOR_ID"),
            inverseJoinColumns = @JoinColumn(name = "MEDICAL_FACILITY_ID"))
    private Set<Facility> facilities = new HashSet<>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.REMOVE)
    private List<Visit> visits = new ArrayList<>();

    public void addFacility(Facility facility) {
        this.facilities.add(facility);
    }
}