package com.radom07.medicalclinic.service;

import com.radom07.medicalclinic.command.CreateUserCommand;
import com.radom07.medicalclinic.exception.UserAlreadyExistsException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.mapper.UserMapper;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.model.entity.User;
import com.radom07.medicalclinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        this.userRepository = Mockito.mock(UserRepository.class);
        this.userMapper = Mappers.getMapper(UserMapper.class);
        this.userService = new UserService(userRepository, userMapper);
    }

    private User buildUser(Long id, String firstName, String lastName, String email, String userName) {
        return User.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .userName(userName)
                .password("1234")
                .build();
    }

    @Test
    void getUsers_DataCorrect_ReturnUsersDto() {
        // given
        List<User> users = List.of(
                buildUser(1L, "Jan", "Kowalski", "jan@test.pl", "jankowalski"),
                buildUser(2L, "Anna", "Nowak", "anna@test.pl", "annanowak")
        );
        when(userRepository.findAll()).thenReturn(users);
        // when
        List<UserDto> userDtos = userService.getUsers();
        // then
        assertAll(
                () -> assertEquals(2, userDtos.size()),
                () -> assertEquals(1L, userDtos.get(0).id()),
                () -> assertEquals("jan@test.pl", userDtos.get(0).email()),
                () -> assertEquals("Anna", userDtos.get(1).firstName())
        );
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getById_DataCorrect_ReturnUserDto() {
        // given
        Long userId = 1L;
        User user = buildUser(userId, "Jan", "Kowalski", "jan@test.pl", "jankowalski");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // when
        UserDto userDto = userService.getById(userId);
        // then
        assertAll(
                () -> assertEquals(1L, userDto.id()),
                () -> assertEquals("jan@test.pl", userDto.email()),
                () -> assertEquals("Jan", userDto.firstName()),
                () -> assertEquals("Kowalski", userDto.lastName())
        );
        verify(userRepository).findById(userId);
    }

    @Test
    void getById_UserNotFound_ThrowsUserNotFoundException() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // when
        UserNotFoundException notFoundException = assertThrows(
                UserNotFoundException.class, () -> userService.getById(userId)
        );
        // then
        assertAll(
                () -> assertEquals("The user with the provided id does not exist", notFoundException.getMessage()),
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundException.getHttpStatus())
        );
    }

    @Test
    void addUser_DataCorrect_ReturnUserDto() {
        // given
        CreateUserCommand command = new CreateUserCommand("Jan", "Kowalski", "jan@test.pl", "jankowalski", "haslo");
        when(userRepository.existsByEmailIgnoreCase(command.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        UserDto userDto = userService.addUser(command);
        // then
        assertAll(
                () -> assertEquals("jan@test.pl", userDto.email()),
                () -> assertEquals("Jan", userDto.firstName()),
                () -> assertEquals("Kowalski", userDto.lastName()),
                () -> assertEquals("jankowalski", userDto.userName())
        );
        verify(userRepository).existsByEmailIgnoreCase(command.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_UserAlreadyExists_ThrowsUserAlreadyExistsException() {
        // given
        CreateUserCommand command = new CreateUserCommand("Jan", "Kowalski", "jan@test.pl", "jankowalski", "haslo");
        when(userRepository.existsByEmailIgnoreCase(command.email())).thenReturn(true);
        // when
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class, () -> userService.addUser(command)
        );
        // then
        assertAll(
                () -> assertEquals("The user with the provided email address already exists", exception.getMessage()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus())
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteById_DataCorrect_DeletesUser() {
        // given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        // when
        userService.deleteById(userId);
        // then
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteById_UserNotFound_ThrowsUserNotFoundException() {
        // given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);
        // when
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class, () -> userService.deleteById(userId)
        );
        // then
        assertEquals("The user with the provided id does not exist", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }
}