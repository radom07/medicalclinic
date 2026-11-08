package com.radom07.medicalclinic.repository;

import com.radom07.medicalclinic.model.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    boolean existsByName(String name);
}