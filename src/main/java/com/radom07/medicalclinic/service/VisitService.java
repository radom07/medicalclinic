package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateVisitCommand;
import com.radom07.medicalclinic.exception.DoctorNotFoundException;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper mapper;

    public List<VisitDto> getAvailableVisits() {
        return visitRepository.findByPatientIsNullAndStartTimeAfter(LocalDateTime.now())
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<VisitDto> getPatientVisits(Long id) {
        return visitRepository.findByPatientId(id)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public VisitDto addVisit(CreateVisitCommand command) {
        TimeVisitValidator.validateNewVisit(command, LocalDateTime.now());
        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("The doctor with the provided id does not exist"));
        if (visitRepository.countOverlappingVisits(doctor.getId(), command.startTime(), command.endTime()) > 0) {
            throw new VisitOverlapException("The doctor already has a scheduled visit");
        }
        return mapper.toDto(visitRepository.save(mapper.toEntity(command, doctor)));
    }

    public VisitDto bookVisit(Long visitId, String patientEmail) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("The visit with the provided id does not exist"));
        TimeVisitValidator.validateTimeInFuture(visit.getStartTime(), LocalDateTime.now());
        if (visit.getPatient() != null) {
            throw new VisitAlreadyBookedException("This visit is already booked");
        }
        Patient patient = patientRepository.findByUserEmailIgnoreCase(patientEmail)
                .orElseThrow(() -> new PatientNotFoundException("The patient with the provided email address does not exist"));
        visit.setPatient(patient);
        return mapper.toDto(visitRepository.save(visit));
    }
}