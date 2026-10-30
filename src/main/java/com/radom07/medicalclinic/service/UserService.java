package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateUserCommand;
import com.radom07.medicalclinic.exception.UserAlreadyExistsException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.mapper.UserMapper;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public UserDto getById(Long id) {
        return userRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("The user with the provided id does not exist"));
    }

    public UserDto addUser(CreateUserCommand command) {
        if (userRepository.existsByEmailIgnoreCase(command.email())) {
            throw new UserAlreadyExistsException("The user with the provided email address already exists");
        }
        return mapper.toDto(userRepository.save(mapper.toEntity(command)));
    }

    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("The user with the provided id does not exist");
        }
        userRepository.deleteById(id);
    }
}
