package com.radom07.medicalclinic.repository;

import com.radom07.medicalclinic.model.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserEmailIgnoreCase(String email);
    void deleteByUserEmail(String email);
    boolean existsByUserEmailIgnoreCase(String email);
    boolean existsByMedicalLicenseNo(String medicalLicenseNo);
}
