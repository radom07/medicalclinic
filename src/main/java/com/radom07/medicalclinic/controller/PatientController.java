package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.ChangePasswordCommand;
import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.command.UpdatePatientCommand;
import com.radom07.medicalclinic.model.dto.PatientDto;
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
    public List<PatientDto> getPatients() {
        return patientService.getPatients();
    }

    @GetMapping("/{email}")
    public PatientDto getPatientByEmail(@PathVariable String email) {
        return patientService.getByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto addPatient(@RequestBody CreatePatientCommand command) {
        return patientService.addPatient(command);
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable String email) {
        patientService.deleteByEmail(email);
    }

    @PutMapping("/{email}")
    public PatientDto updatePatient(@PathVariable String email, @RequestBody UpdatePatientCommand command) {
        return patientService.updatePatient(email, command);
    }

    @PatchMapping("/{email}/password")
    public PatientDto updatePassword(@PathVariable String email, @RequestBody ChangePasswordCommand command) {
        return patientService.updatePassword(email, command.password());
    }
}
