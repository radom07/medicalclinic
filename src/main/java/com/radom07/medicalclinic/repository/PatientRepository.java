package com.radom07.medicalclinic.repository;

import com.radom07.medicalclinic.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmailIgnoreCase(String email);

    void deleteByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByIdCardNo(String idCardNo);
}
