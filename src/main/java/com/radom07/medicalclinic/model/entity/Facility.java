package com.radom07.medicalclinic.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "MEDICAL_FACILITIES")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;
    @Column(name = "CITY", nullable = false)
    private String city;
    @Column(name = "POSTAL_CODE")
    private String postalCode;
    @Column(name = "STREET")
    private String street;
    @Column(name = "BUILDING_NUMBER")
    private String buildingNo;
    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;
    @ManyToMany(mappedBy = "facilities", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<Doctor> doctors;
}