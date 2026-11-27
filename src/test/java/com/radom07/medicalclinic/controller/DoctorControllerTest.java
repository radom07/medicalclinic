package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.AssignDoctorToFacilityCommand;
import com.radom07.medicalclinic.command.CreateDoctorCommand;
import com.radom07.medicalclinic.exception.DoctorAlreadyAssignedToFacility;
import com.radom07.medicalclinic.exception.DoctorAlreadyExistsException;
import com.radom07.medicalclinic.exception.DoctorNotFoundException;
import com.radom07.medicalclinic.exception.FacilityNotFoundException;
import com.radom07.medicalclinic.exception.UserNotFoundException;
import com.radom07.medicalclinic.model.Specialization;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private DoctorService doctorService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getDoctors_DataCorrect_Response200() throws Exception {
        // given
        List<DoctorDto> doctors = List.of(
                new DoctorDto(1L, new UserDto(1L, "Jan", "Kowalski", "jan@test.pl", "jankowalski"),
                        "CARDIOLOGIST", List.of("Centrum Medyczne Warszawa", "Przychodnia Radom")),
                new DoctorDto(2L, new UserDto(2L, "Anna", "Nowak", "anna@test.pl", "annanowak"),
                        "SURGEON", List.of())
        );
        when(doctorService.getDoctors(any(Pageable.class))).thenReturn(new PageImpl<>(doctors));
        // when & then
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].user.id").value(1))
                .andExpect(jsonPath("$.content[0].user.email").value("jan@test.pl"))
                .andExpect(jsonPath("$.content[0].user.firstName").value("Jan"))
                .andExpect(jsonPath("$.content[0].user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.content[0].user.userName").value("jankowalski"))
                .andExpect(jsonPath("$.content[0].specialization").value("CARDIOLOGIST"))
                .andExpect(jsonPath("$.content[0].facilitiesNames.length()").value(2))
                .andExpect(jsonPath("$.content[0].facilitiesNames[0]").value("Centrum Medyczne Warszawa"))
                .andExpect(jsonPath("$.content[0].facilitiesNames[1]").value("Przychodnia Radom"))
                .andExpect(jsonPath("$.content[1].user.lastName").value("Nowak"))
                .andExpect(jsonPath("$.content[1].specialization").value("SURGEON"))
                .andExpect(jsonPath("$.content[1].facilitiesNames.length()").value(0));
        verify(doctorService).getDoctors(any(Pageable.class));
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    void getDoctorByEmail_DataCorrect_Response200() throws Exception {
        // given
        String email = "jan@test.pl";
        DoctorDto doctorDto = new DoctorDto(1L, new UserDto(1L, "Jan", "Kowalski", "jan@test.pl", "jankowalski"),
                "CARDIOLOGIST", List.of("Centrum Medyczne Warszawa"));
        when(doctorService.getByEmail(email)).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(get("/doctors/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("jan@test.pl"))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.user.userName").value("jankowalski"))
                .andExpect(jsonPath("$.specialization").value("CARDIOLOGIST"))
                .andExpect(jsonPath("$.facilitiesNames.length()").value(1))
                .andExpect(jsonPath("$.facilitiesNames[0]").value("Centrum Medyczne Warszawa"));
        verify(doctorService).getByEmail(email);
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    void getDoctorByEmail_DoctorNotFound_Response404() throws Exception {
        // given
        String email = "jan@test.pl";
        when(doctorService.getByEmail(email))
                .thenThrow(new DoctorNotFoundException("The doctor with the provided email address does not exist"));
        // when & then
        mockMvc.perform(get("/doctors/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The doctor with the provided email address does not exist"))
                .andExpect(jsonPath("$.path").value("/doctors/jan@test.pl"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getDoctorByEmail_InvalidEmail_Response400() throws Exception {
        // given
        String invalidEmail = "not-an-email";
        // when & then
        mockMvc.perform(get("/doctors/{email}", invalidEmail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for request parameters"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.path").value("/doctors/not-an-email"))
                .andExpect(jsonPath("$.timestamp").exists());
        verifyNoInteractions(doctorService);
    }

    @Test
    void addDoctor_DataCorrect_Response201() throws Exception {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(1L, Specialization.CARDIOLOGIST, "ABC1234567", List.of(1L));
        DoctorDto doctorDto = new DoctorDto(1L, new UserDto(1L, "Jan", "Kowalski", "jan@test.pl", "jankowalski"),
                "CARDIOLOGIST", List.of("Centrum Medyczne Warszawa"));
        when(doctorService.addDoctor(command)).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(post("/doctors")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("jan@test.pl"))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.user.userName").value("jankowalski"))
                .andExpect(jsonPath("$.specialization").value("CARDIOLOGIST"))
                .andExpect(jsonPath("$.facilitiesNames[0]").value("Centrum Medyczne Warszawa"));
        verify(doctorService).addDoctor(command);
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    void addDoctor_DoctorAlreadyExists_Response409() throws Exception {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(1L, Specialization.CARDIOLOGIST, "ABC1234567", List.of(1L));
        when(doctorService.addDoctor(command))
                .thenThrow(new DoctorAlreadyExistsException("The doctor with the provided medicalLicenseNumber already exists"));
        // when & then
        mockMvc.perform(post("/doctors")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("The doctor with the provided medicalLicenseNumber already exists"))
                .andExpect(jsonPath("$.path").value("/doctors"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addDoctor_UserNotFound_Response404() throws Exception {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(99L, Specialization.CARDIOLOGIST, "ABC1234567", List.of(1L));
        when(doctorService.addDoctor(command))
                .thenThrow(new UserNotFoundException("The user with the provided id does not exist"));
        // when & then
        mockMvc.perform(post("/doctors")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The user with the provided id does not exist"))
                .andExpect(jsonPath("$.path").value("/doctors"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addDoctor_FacilityNotFound_Response404() throws Exception {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand(1L, Specialization.CARDIOLOGIST, "ABC1234567", List.of(1L, 99L));
        when(doctorService.addDoctor(command))
                .thenThrow(new FacilityNotFoundException("One or more facilities not found"));
        // when & then
        mockMvc.perform(post("/doctors")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("One or more facilities not found"))
                .andExpect(jsonPath("$.path").value("/doctors"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deleteDoctorByEmail_DataCorrect_Response204() throws Exception {
        // given
        String email = "jan@test.pl";
        // when & then
        mockMvc.perform(delete("/doctors/{email}", email))
                .andExpect(status().isNoContent());
        verify(doctorService).deleteByEmail(email);
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    void deleteDoctorByEmail_DoctorNotFound_Response404() throws Exception {
        // given
        String email = "jan@test.pl";
        doThrow(new DoctorNotFoundException("The doctor with the provided email address does not exist"))
                .when(doctorService).deleteByEmail(email);
        // when & then
        mockMvc.perform(delete("/doctors/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The doctor with the provided email address does not exist"))
                .andExpect(jsonPath("$.path").value("/doctors/jan@test.pl"))
                .andExpect(jsonPath("$.timestamp").exists());
        verify(doctorService).deleteByEmail(email);
    }

    @Test
    void assignDoctorToFacility_DataCorrect_Response200() throws Exception {
        // given
        Long doctorId = 1L;
        AssignDoctorToFacilityCommand command = new AssignDoctorToFacilityCommand(2L);
        DoctorDto doctorDto = new DoctorDto(1L, new UserDto(1L, "Jan", "Kowalski", "jan@test.pl", "jankowalski"),
                "CARDIOLOGIST", List.of("Przychodnia Radom"));
        when(doctorService.assignDoctorToFacility(doctorId, command.facilityId())).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(post("/doctors/{id}/facilities/assign", doctorId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.email").value("jan@test.pl"))
                .andExpect(jsonPath("$.specialization").value("CARDIOLOGIST"))
                .andExpect(jsonPath("$.facilitiesNames.length()").value(1))
                .andExpect(jsonPath("$.facilitiesNames[0]").value("Przychodnia Radom"));
        verify(doctorService).assignDoctorToFacility(doctorId, command.facilityId());
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    void assignDoctorToFacility_DoctorNotFound_Response404() throws Exception {
        // given
        Long doctorId = 99L;
        AssignDoctorToFacilityCommand command = new AssignDoctorToFacilityCommand(2L);
        when(doctorService.assignDoctorToFacility(doctorId, command.facilityId()))
                .thenThrow(new DoctorNotFoundException("The doctor with the provided id does not exist"));
        // when & then
        mockMvc.perform(post("/doctors/{id}/facilities/assign", doctorId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The doctor with the provided id does not exist"))
                .andExpect(jsonPath("$.path").value("/doctors/99/facilities/assign"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void assignDoctorToFacility_FacilityNotFound_Response404() throws Exception {
        // given
        Long doctorId = 1L;
        AssignDoctorToFacilityCommand command = new AssignDoctorToFacilityCommand(99L);
        when(doctorService.assignDoctorToFacility(doctorId, command.facilityId()))
                .thenThrow(new FacilityNotFoundException("The medical facility with provided id does not exist"));
        // when & then
        mockMvc.perform(post("/doctors/{id}/facilities/assign", doctorId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The medical facility with provided id does not exist"))
                .andExpect(jsonPath("$.path").value("/doctors/1/facilities/assign"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void assignDoctorToFacility_DoctorAlreadyAssigned_Response409() throws Exception {
        // given
        Long doctorId = 1L;
        AssignDoctorToFacilityCommand command = new AssignDoctorToFacilityCommand(2L);
        when(doctorService.assignDoctorToFacility(doctorId, command.facilityId()))
                .thenThrow(new DoctorAlreadyAssignedToFacility("The doctor is already assigned to facility"));
        // when & then
        mockMvc.perform(post("/doctors/{id}/facilities/assign", doctorId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("The doctor is already assigned to facility"))
                .andExpect(jsonPath("$.path").value("/doctors/1/facilities/assign"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}