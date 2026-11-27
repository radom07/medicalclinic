package com.radom07.medicalclinic.controller;

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
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patients")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PatientController {

    private final PatientService patientService;

    @Operation(
            summary = "Get paginated list of patients",
            description = "Retrieves a paginated and sorted list of patients registered in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a page of patients"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters")
    })
    @GetMapping
    public PagedModel<PatientDto> getPatients(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        log.info("Received GET /patients request");
        return new PagedModel<>(patientService.getPatients(pageable));
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
        log.info("Received GET /patients/{} request", email);
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
            @ApiResponse(responseCode = "404", description = "User with provided ID does not exist",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "Patient with provided ID card already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto addPatient(@Valid @RequestBody CreatePatientCommand command) {
        log.info("Received POST /patients request for command={}", command);
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
        log.info("Received DELETE /patients/{} request", email);
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
        log.info("Received PUT /patients/{} request with command={}", email, command);
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
        log.info("Received PATCH /patients/{}/password request with command={}", email, command);
        return patientService.updatePassword(email, command.password());
    }
}