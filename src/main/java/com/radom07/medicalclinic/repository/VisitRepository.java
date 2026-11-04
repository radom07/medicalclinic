package com.radom07.medicalclinic.repository;

import com.radom07.medicalclinic.model.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    List<Visit> findByPatientIsNullAndStartTimeAfter(LocalDateTime time);

    List<Visit> findByPatientUserEmailIgnoreCase(String email);

    @Query(
            "SELECT COUNT(v) FROM Visit v "
                    + "WHERE v.doctor.id = :doctorId "
                    + "AND v.startTime < :endTime AND v.endTime > :startTime"
    )
    long countOverlappingVisits(@Param("doctorId") Long doctorId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);
}
