package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.model.Patient;
import com.radom07.medicalclinic.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    List<Patient> getPatients() {
        return patientService.getPatients();
    }

    @GetMapping("/{email}")
    public Patient getPatientByEmail(@PathVariable String email) {
        return patientService.getByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient addPatient(@RequestBody Patient patient) {
        return patientService.addPatient(patient);
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable String email) {
        patientService.deleteByEmail(email);
    }

    @PutMapping("/{email}")
    public Patient updatePatient(@PathVariable String email, @RequestBody Patient patient) {
        return patientService.updatePatient(email, patient);
    }

    @PatchMapping("/{email}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(@PathVariable String email, @RequestBody Patient patient) {
        patientService.updatePassword(email, patient.getPassword());
    }
}
