package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateUserCommand;
import com.radom07.medicalclinic.exception.UserAlreadyExistsException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.mapper.UserMapper;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(mapper::toDto);
    }

    public UserDto getById(Long id) {
        return userRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("The user with the provided id does not exist"));
    }

    public UserDto addUser(CreateUserCommand command) {
        log.info("Creating user with command={}", command);
        if (userRepository.existsByEmailIgnoreCase(command.email())) {
            throw new UserAlreadyExistsException("The user with the provided email address already exists");
        }
        UserDto created = mapper.toDto(userRepository.save(mapper.toEntity(command)));
        log.info("User created with id={}", created.id());
        return created;
    }

    public void deleteById(Long id) {
        log.info("Deleting user id={}", id);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("The user with the provided id does not exist");
        }
        userRepository.deleteById(id);
        log.info("User id={} deleted", id);
    }
}