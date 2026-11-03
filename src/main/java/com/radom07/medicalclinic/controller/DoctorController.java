package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.AssignDoctorToFacilityCommand;
import com.radom07.medicalclinic.command.AssignDoctorToUserCommand;
import com.radom07.medicalclinic.command.CreateDoctorCommand;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.service.DoctorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public List<DoctorDto> getDoctors() {
        return doctorService.getDoctors();
    }

    @GetMapping("/{email}")
    public DoctorDto getDoctorByEmail(@PathVariable @Email String email) {
        return doctorService.getByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto addDoctor(@Valid @RequestBody CreateDoctorCommand command) {
        return doctorService.addDoctor(command);
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoctorByEmail(@PathVariable @Email String email) {
        doctorService.deleteByEmail(email);
    }

    @PatchMapping("/facilities")
    public DoctorDto assignDoctorToFacility(@Valid @RequestBody AssignDoctorToFacilityCommand command) {
        return doctorService.assignDoctorToFacility(command);
    }

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto assignDoctorToUser(@Valid @RequestBody AssignDoctorToUserCommand command) {
        return doctorService.assignDoctorToUser(command);
    }
}
