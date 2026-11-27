package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateVisitCommand;
import com.radom07.medicalclinic.exception.DoctorNotFoundException;
import com.radom07.medicalclinic.exception.InvalidVisitFilterException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.exception.VisitAlreadyBookedException;
import com.radom07.medicalclinic.exception.VisitNotFoundException;
import com.radom07.medicalclinic.exception.VisitOverlapException;
import com.radom07.medicalclinic.mapper.VisitMapper;
import com.radom07.medicalclinic.model.dto.VisitDto;
import com.radom07.medicalclinic.model.entity.Doctor;
import com.radom07.medicalclinic.model.entity.Patient;
import com.radom07.medicalclinic.model.entity.Visit;
import com.radom07.medicalclinic.repository.DoctorRepository;
import com.radom07.medicalclinic.repository.PatientRepository;
import com.radom07.medicalclinic.repository.VisitRepository;
import com.radom07.medicalclinic.validator.TimeVisitValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitService {

    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper mapper;

    public Page<VisitDto> getVisitsByCriteria(Long patientId, boolean onlyAvailable, Pageable pageable) {
        log.info("fetching data visits with criteria: patientId={}, onlyAvailable={}", patientId, onlyAvailable);
        if (patientId != null && onlyAvailable) {
            throw new InvalidVisitFilterException();
        }
        if (patientId != null) {
            return visitRepository.findByPatientId(patientId, pageable).map(mapper::toDto);
        }
        if (onlyAvailable) {
            return visitRepository.findByPatientIsNullAndStartTimeAfter(LocalDateTime.now(), pageable).map(mapper::toDto);
        }
        return visitRepository.findAll(pageable).map(mapper::toDto);
    }

    public VisitDto addVisit(CreateVisitCommand command) {
        log.info("Creating new visit for command={}", command);
        TimeVisitValidator.validateNewVisit(command, LocalDateTime.now());
        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("The doctor with the provided id does not exist"));
        if (visitRepository.countOverlappingVisits(doctor.getId(), command.startTime(), command.endTime()) > 0) {
            throw new VisitOverlapException("The doctor already has a scheduled visit");
        }
        VisitDto created = mapper.toDto(visitRepository.save(mapper.toEntity(command, doctor)));
        log.info("Created visit with id={} for doctorId={}", created.id(), created.doctor().id());
        return created;
    }

    @Transactional
    public VisitDto bookVisit(Long visitId, Long patientId) {
        log.info("Booking new visit for visitId={} and patientId={}", visitId, patientId);
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("The visit with the provided id does not exist"));
        TimeVisitValidator.validateTimeInFuture(visit.getStartTime(), LocalDateTime.now());
        if (visit.getPatient() != null) {
            throw new VisitAlreadyBookedException("This visit is already booked");
        }
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided id does not exist"));
        visit.setPatient(patient);
        VisitDto booked = mapper.toDto(visitRepository.save(visit));
        log.info("Booked visit with id={}", booked.id());
        return booked;
    }
}