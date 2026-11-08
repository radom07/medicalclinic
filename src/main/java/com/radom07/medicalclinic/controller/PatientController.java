package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.AssignPatientToUserCommand;
import com.radom07.medicalclinic.command.ChangePasswordCommand;
import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.command.UpdatePatientCommand;
import com.radom07.medicalclinic.model.dto.ErrorMessageDto;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patients")
@RequiredArgsConstructor
@Validated
public class PatientController {

    private final PatientService patientService;

    @Operation(summary = "Get all patients", description = "Returns all patients in system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all patients")
            // content z @ArraySchema czy nie ma potrzeby?
    })
    @GetMapping
    public List<PatientDto> getPatients() {
        return patientService.getPatients();
    }

    @Operation(summary = "Get a patient by email", description = "Returns a patient with provided email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the patient",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid email supplied",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @GetMapping("/{email}")
    public PatientDto getPatientByEmail(@PathVariable("email")
                                        @Parameter(
                                                name = "email",
                                                description = "Patient's email",
                                                example = "jan@test.pl")
                                        @Email String email) {
        return patientService.getByEmail(email);
    }

    @Operation(summary = "Create a new patient", description = "Creates a patient profile in system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "Patient with provided email or ID card already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto addPatient(@Valid @RequestBody CreatePatientCommand command) {
        return patientService.addPatient(command);
    }

    @Operation(summary = "Delete a patient by email", description = "Deletes a patient from system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email supplied",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatientByEmail(@PathVariable("email")
                                     @Parameter(
                                             name = "email",
                                             description = "Patient's email",
                                             example = "jan@test.pl")
                                     @Email String email) {
        patientService.deleteByEmail(email);
    }

    @Operation(summary = "Update patient data", description = "Updates an existing patient's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "Patient with provided email already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PutMapping("/{email}")
    public PatientDto updatePatient(@PathVariable("email")
                                    @Parameter(
                                            name = "email",
                                            description = "Patient's email",
                                            example = "jan@test.pl")
                                    @Email String email,
                                    @Valid @RequestBody UpdatePatientCommand command) {
        return patientService.updatePatient(email, command);
    }

    @Operation(summary = "Update patient's password", description = "Updates the password for a specific patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PatchMapping("/{email}/password")
    public PatientDto updatePassword(@PathVariable("email")
                                     @Parameter(
                                             name = "email",
                                             description = "Patient's email",
                                             example = "jan@test.pl")
                                     @Email String email,
                                     @Valid @RequestBody ChangePasswordCommand command) {
        return patientService.updatePassword(email, command.password());
    }

    @Operation(summary = "Assign patient profile to user", description = "Creates a patient profile for an existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient profile assigned successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "Patient with the provided IdCardNo already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto assignPatientToUser(@Valid @RequestBody AssignPatientToUserCommand command) {
        return patientService.assignPatientToUser(command);
    }
}