package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.CreateUserCommand;
import com.radom07.medicalclinic.exception.UserAlreadyExistsException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUsers_DataCorrect_Response200() throws Exception {
        // given
        List<UserDto> users = List.of(
                new UserDto(1L, "Jan", "Kowalski", "jan@test.pl", "jankowalski"),
                new UserDto(2L, "Anna", "Nowak", "anna@test.pl", "annanowak")
        );
        when(userService.getUsers()).thenReturn(users);
        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].firstName").value("Jan"))
                .andExpect(jsonPath("$.[1].email").value("anna@test.pl"));
        verify(userService).getUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserById_DataCorrect_Response200() throws Exception {
        // given
        Long userId = 1L;
        UserDto userDto = new UserDto(userId, "Jan", "Kowalski", "jan@test.pl", "jankowalski");
        when(userService.getById(userId)).thenReturn(userDto);
        // when & then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("jan@test.pl"))
                .andExpect(jsonPath("$.firstName").value("Jan"));
        verify(userService).getById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserById_UserNotFound_Response404() throws Exception {
        // given
        Long userId = 1L;
        when(userService.getById(userId))
                .thenThrow(new UserNotFoundException("The user with the provided id does not exist"));
        // when & then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The user with the provided id does not exist"));
    }

    @Test
    void addUser_DataCorrect_Response201() throws Exception {
        // given
        CreateUserCommand command = new CreateUserCommand("Jan", "Kowalski", "jan@test.pl", "jankowalski", "haslo123");
        UserDto userDto = new UserDto(1L, "Jan", "Kowalski", "jan@test.pl", "jankowalski");
        when(userService.addUser(command)).thenReturn(userDto);
        // when & then
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("jan@test.pl"));
        verify(userService).addUser(command);
    }

    @Test
    void addUser_UserAlreadyExists_Response409() throws Exception {
        // given
        CreateUserCommand command = new CreateUserCommand("Jan", "Kowalski", "jan@test.pl", "jankowalski", "haslo123");
        when(userService.addUser(command))
                .thenThrow(new UserAlreadyExistsException("The user with the provided email address already exists"));
        // when & then
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("The user with the provided email address already exists"));
    }

    @Test
    void deleteUserById_DataCorrect_Response204() throws Exception {
        // given
        Long userId = 1L;
        // when & then
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());
        verify(userService).deleteById(userId);
    }

    @Test
    void deleteUserById_UserNotFound_Response404() throws Exception {
        // given
        Long userId = 1L;
        doThrow(new UserNotFoundException("The user with the provided id does not exist"))
                .when(userService).deleteById(userId);
        // when & then
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The user with the provided id does not exist"));
    }
}