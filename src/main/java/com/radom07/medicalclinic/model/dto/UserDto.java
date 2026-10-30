package com.radom07.medicalclinic.model.dto;

import lombok.Builder;

@Builder
public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String userName
) {
}
