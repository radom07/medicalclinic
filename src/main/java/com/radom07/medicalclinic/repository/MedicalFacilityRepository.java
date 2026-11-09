package com.radom07.medicalclinic.repository;

import com.radom07.medicalclinic.model.entity.MedicalFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalFacilityRepository extends JpaRepository<MedicalFacility, Long> {
    Optional<MedicalFacility> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
}
