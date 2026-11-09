package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.BookVisitCommand;
import com.radom07.medicalclinic.command.CreateVisitCommand;
import com.radom07.medicalclinic.model.dto.ErrorMessageDto;
import com.radom07.medicalclinic.model.dto.VisitDto;
import com.radom07.medicalclinic.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/visits")
@Tag(name = "Visits")
@RequiredArgsConstructor
@Validated
public class VisitController {

    private final VisitService visitService;

    @Operation(summary = "Get all visits", description = "Returns all visits, can be filtered")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all visits")
    })
    @GetMapping()
    public List<VisitDto> getVisits(
            @Parameter(description = "Filter by patient's id")
            @RequestParam(name = "patientId", required = false) Long patientId,
            @Parameter(description = "Set to true to get only available visits")
            @RequestParam(name = "available", required = false, defaultValue = "false") boolean available
    ) {
        return visitService.getVisitsByCriteria(patientId, available);
    }

    @Operation(summary = "Create a new visit", description = "Creates an available visit slot for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Visit created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = VisitDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided or invalid visit time",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "Doctor already has a visit scheduled in the provided time",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto addVisit(@Valid @RequestBody CreateVisitCommand command) {
        return visitService.addVisit(command);
    }

    @Operation(summary = "Book a visit", description = "Books an available visit for a patient with provided email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Visit booked successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = VisitDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "404", description = "Visit or patient not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "Visit is already booked",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping("/{id}/book")
    public VisitDto bookVisit(@PathVariable("id")
                              @Parameter(
                                      name = "id",
                                      description = "Visit's id",
                                      example = "1")
                              Long id,
                              @Valid @RequestBody BookVisitCommand command) {
        return visitService.bookVisit(id, command.patientId());
    }
}