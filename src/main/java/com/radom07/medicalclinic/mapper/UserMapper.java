package com.radom07.medicalclinic.mapper;

import com.radom07.medicalclinic.command.CreateUserCommand;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(CreateUserCommand command);
    UserDto toDto(User user);
}
