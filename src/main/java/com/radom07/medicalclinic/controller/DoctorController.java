package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.AssignDoctorToFacilityCommand;
import com.radom07.medicalclinic.command.AssignDoctorToUserCommand;
import com.radom07.medicalclinic.command.CreateDoctorCommand;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.model.dto.ErrorMessageDto;
import com.radom07.medicalclinic.service.DoctorService;
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
@Tag(name = "Doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Get all doctors", description = "Returns all doctors in system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all doctors")
    })
    @GetMapping
    public List<DoctorDto> getDoctors() {
        return doctorService.getDoctors();
    }

    @Operation(summary = "Get a doctor by email", description = "Returns a doctor with provided email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the doctor",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DoctorDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid email supplied",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @GetMapping("/{email}")
    public DoctorDto getDoctorByEmail(@PathVariable("email")
                                      @Parameter(
                                              name = "email",
                                              description = "Doctor's email",
                                              example = "jan@test.pl")
                                      @Email String email) {
        return doctorService.getByEmail(email);
    }

    @Operation(summary = "Create a new doctor", description = "Creates a doctor profile in system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DoctorDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "One or more medical provided facilities not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "Doctor with provided email or medical license number already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto addDoctor(@Valid @RequestBody CreateDoctorCommand command) {
        return doctorService.addDoctor(command);
    }

    @Operation(summary = "Delete a doctor by email", description = "Deletes a doctor from system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doctor deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email supplied",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoctorByEmail(@PathVariable("email")
                                    @Parameter(
                                            name = "email",
                                            description = "Doctor's email",
                                            example = "jan@test.pl")
                                    @Email String email) {
        doctorService.deleteByEmail(email);
    }

    @Operation(summary = "Assign doctor to facility", description = "Assigns an existing doctor to an existing facility")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor assigned to facility successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DoctorDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Doctor or medical facility not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PatchMapping("/facilities")
    public DoctorDto assignDoctorToFacility(@Valid @RequestBody AssignDoctorToFacilityCommand command) {
        return doctorService.assignDoctorToFacility(command);
    }

    @Operation(summary = "Assign doctor profile to user", description = "Creates a doctor profile for an existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor profile assigned successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DoctorDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "User already has a doctor profile or doctor with the provided medical license number already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto assignDoctorToUser(@Valid @RequestBody AssignDoctorToUserCommand command) {
        return doctorService.assignDoctorToUser(command);
    }
}