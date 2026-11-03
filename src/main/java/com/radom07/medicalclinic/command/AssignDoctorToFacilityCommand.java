package com.radom07.medicalclinic.command;

import jakarta.validation.constraints.NotNull;

public record AssignDoctorToFacilityCommand(
        @NotNull Long doctorId,
        @NotNull Long facilityId
) {
}