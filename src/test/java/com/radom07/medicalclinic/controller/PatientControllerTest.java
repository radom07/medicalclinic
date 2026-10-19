package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.ChangePasswordCommand;
import com.radom07.medicalclinic.command.CreatePatientCommand;
import com.radom07.medicalclinic.command.UpdatePatientCommand;
import com.radom07.medicalclinic.exception.PatientAlreadyExistsException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PatientService patientService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPatients_DataCorrect_ReturnPatientsDto() throws Exception {
        // given
        List<PatientDto> patients = List.of(
                new PatientDto("jan@test.pl", "Jan", "Kowalski",
                        "111222333", LocalDate.of(2015, 10, 25)),
                new PatientDto("anna@test.pl", "Anna", "Nowak",
                        "444555666", LocalDate.of(2018, 5, 12)),
                new PatientDto("piotr@test.pl", "Piotr", "Wiśniewski",
                        "555666777", LocalDate.of(2020, 1, 15))
        );
        when(patientService.getPatients()).thenReturn(patients);
        // when & then
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].email").value("jan@test.pl"))
                .andExpect(jsonPath("$.[1].email").value("anna@test.pl"))
                .andExpect(jsonPath("$.[2].email").value("piotr@test.pl"));
    }

    @Test
    void getPatientByEmail_DataCorrect_ReturnPatientDto() throws Exception {
        // given
        String email = "piotr@test.pl";
        PatientDto patientDto = new PatientDto("piotr@test.pl", "Piotr", "Wiśniewski",
                "555666777", LocalDate.of(2020, 1, 15));
        when(patientService.getByEmail(email)).thenReturn(patientDto);
        // when & then
        mockMvc.perform(get("/patients/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("piotr@test.pl"))
                .andExpect(jsonPath("$.firstName").value("Piotr"));
    }

    @Test
    void getPatientByEmail_PatientNotFound_ReturnNotFoundStatus() throws Exception {
        String email = "piotr@test.pl";
        when(patientService.getByEmail(email)).thenThrow(new PatientNotFoundException("The patient with the provided email address does not exist"));
        // when & then
        mockMvc.perform(get("/patients/{email}", email))
                .andExpect(status().isNotFound());
    }

    @Test
    void addPatient_DataCorrect_ReturnCreatedStatus() throws Exception {
        // given
        CreatePatientCommand command = new CreatePatientCommand("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25));
        PatientDto patientDto = new PatientDto("test@test.pl", "Jan", "Kowalski",
                "123456789", LocalDate.of(2015, 10, 25));
        when(patientService.addPatient(command)).thenReturn(patientDto);
        // when & then
        mockMvc.perform(post("/patients").content(objectMapper.writeValueAsString(command)).contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.pl"))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.birthday").value("2015-10-25"));
    }

    @Test
    void addPatient_PatientAlreadyExists_ReturnBadRequestStatus() throws Exception {
        // given
        CreatePatientCommand command = new CreatePatientCommand("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25));
        when(patientService.addPatient(command)).thenThrow(new PatientAlreadyExistsException("The patient with the provided email address already exists"));
        // when & then
        mockMvc.perform(post("/patients").content(objectMapper.writeValueAsString(command)).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The patient with the provided email address already exists"));
    }

    @Test
    void deletePatient_DataCorrect_ReturnNoContentStatus() throws Exception {
        // given
        String email = "test@test.pl";
        // when & then
        mockMvc.perform(delete("/patients/{email}", email))
                .andExpect(status().isNoContent());
        verify(patientService).deleteByEmail(email);
    }

    @Test
    void deletePatient_PatientNotFound_ReturnNotFoundStatus() throws Exception {
        // given
        String email = "test@test.pl";
        doThrow(new PatientNotFoundException("The patient with the provided email address does not exist"))
                .when(patientService).deleteByEmail(email);
        // when & then
        mockMvc.perform(delete("/patients/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("The patient with the provided email address does not exist"));
        verify(patientService).deleteByEmail(email);
    }

    @Test
    void updatePatient_DataCorrect_ReturnPatientDto() throws Exception {
        // given
        String email = "test@test.pl";
        UpdatePatientCommand command = new UpdatePatientCommand("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25));
        PatientDto patientDto = new PatientDto("test@test.pl", "Jan", "Kowalski",
                "123456789", LocalDate.of(2015, 10, 25));
        when(patientService.updatePatient(email, command)).thenReturn(patientDto);
        // when & then
        mockMvc.perform(put("/patients/{email}", email).content(objectMapper.writeValueAsString(command)).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.pl"))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.birthday").value("2015-10-25"));
    }

    @Test
    void updatePatient_PatientNotFound_ReturnNotFoundStatus() throws Exception {
        // given
        String email = "test@test.pl";
        UpdatePatientCommand command = new UpdatePatientCommand("test@test.pl", "1234", "0001", "Jan",
                "Kowalski", "123456789", LocalDate.of(2015, 10, 25));
        when(patientService.updatePatient(email, command)).thenThrow(new PatientNotFoundException("The patient with the provided email address does not exist"));
        // when & then
        mockMvc.perform(put("/patients/{emial}", email).content(objectMapper.writeValueAsString(command)).contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("The patient with the provided email address does not exist"));
    }

    @Test
    void updatePassword_DataCorrect_ReturnPatientDto() throws Exception {
        // given
        ChangePasswordCommand command = new ChangePasswordCommand("0000");
        String email = "test@test.pl";
        PatientDto patientDto = new PatientDto("test@test.pl", "Jan", "Kowalski",
                "123456789", LocalDate.of(2015, 10, 25));
        when(patientService.updatePassword(anyString(), anyString())).thenReturn(patientDto);
        // when & then
        mockMvc.perform(patch("/patients/{email}/password", email).content(objectMapper.writeValueAsString(command)).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.pl"));
    }

    @Test
    void updatePassword_PatientNotFound_ReturnNotFoundStatus() throws Exception {
        // given
        ChangePasswordCommand command = new ChangePasswordCommand("0000");
        String email = "test@test.pl";
        when(patientService.updatePassword(anyString(), anyString())).thenThrow(new PatientNotFoundException("The patient with the provided email address does not exist"));
        // when & then
        mockMvc.perform(patch("/patients/{email}/password", email).content(objectMapper.writeValueAsString(command)).contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("The patient with the provided email address does not exist"));
    }
}