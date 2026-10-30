package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.CreatePatientProfileCommand;
import com.radom07.medicalclinic.command.CreateUserCommand;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.service.PatientService;
import com.radom07.medicalclinic.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final PatientService patientService;

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    // GET /users/abc zwróci 500
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody CreateUserCommand command) {
        return userService.addUser(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @PostMapping("/{id}/patient")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto createPatientProfileFromUser(@PathVariable Long id, @Valid @RequestBody CreatePatientProfileCommand command) {
        return patientService.createPatientProfile(id, command);
    }
}
