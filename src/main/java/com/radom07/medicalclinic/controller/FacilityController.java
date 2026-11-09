package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.CreateFacilityCommand;
import com.radom07.medicalclinic.model.dto.ErrorMessageDto;
import com.radom07.medicalclinic.model.dto.FacilityDto;
import com.radom07.medicalclinic.service.FacilityService;
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
@RequestMapping("/facilities")
@Tag(name = "Facilities")
@RequiredArgsConstructor
@Validated
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(summary = "Get all facilities", description = "Returns all facilities in system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all facilities")
    })
    @GetMapping
    public List<FacilityDto> getFacilities() {
        return facilityService.getFacilities();
    }

    @Operation(summary = "Get a facility by id", description = "Returns a facility with provided id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the facility",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = FacilityDto.class))}),
            @ApiResponse(responseCode = "404", description = "Facility not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @GetMapping("/{id}")
    public FacilityDto getFacilityById(@PathVariable("id")
                                       @Parameter(
                                               name = "id",
                                               description = "Facility's id",
                                               example = "1")
                                       Long id) {
        return facilityService.getFacilityById(id);
    }

    @Operation(summary = "Create a new facility", description = "Creates a facility in system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Facility created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = FacilityDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "Facility with provided name already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityDto addFacility(@Valid @RequestBody CreateFacilityCommand command) {
        return facilityService.addFacility(command);
    }

    @Operation(summary = "Delete a facility by id", description = "Deletes a facility from system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Facility deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Facility not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFacilityById(@PathVariable("id")
                                   @Parameter(
                                           name = "id",
                                           description = "Facility's id",
                                           example = "1")
                                   Long id) {
        facilityService.deleteFacilityById(id);
    }
}