package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.BookVisitCommand;
import com.radom07.medicalclinic.command.CreateVisitCommand;
import com.radom07.medicalclinic.exception.DoctorNotFoundException;
import com.radom07.medicalclinic.exception.InvalidVisitFilterException;
import com.radom07.medicalclinic.exception.InvalidVisitTimeException;
import com.radom07.medicalclinic.exception.PatientNotFoundException;
import com.radom07.medicalclinic.exception.VisitAlreadyBookedException;
import com.radom07.medicalclinic.exception.VisitNotFoundException;
import com.radom07.medicalclinic.exception.VisitOverlapException;
import com.radom07.medicalclinic.model.dto.DoctorDto;
import com.radom07.medicalclinic.model.dto.PatientDto;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.model.dto.VisitDto;
import com.radom07.medicalclinic.service.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitController.class)
@AutoConfigureMockMvc
class VisitControllerTest {

    private static final LocalDateTime START_TIME = LocalDateTime.of(2099, 12, 1, 9, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2099, 12, 1, 9, 30);

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private VisitService visitService;
    @Autowired
    private ObjectMapper objectMapper;

    private DoctorDto buildDoctorDto() {
        return new DoctorDto(1L, new UserDto(1L, "Jan", "Kowalski", "jan@test.pl", "jankowalski"),
                "CARDIOLOGIST", List.of("Centrum Medyczne Warszawa"));
    }

    private PatientDto buildPatientDto() {
        return new PatientDto(1L, new UserDto(2L, "Anna", "Nowak", "anna@test.pl", "annanowak"),
                "123456789", LocalDate.of(1990, 5, 12));
    }

    @Test
    void getVisits_NoFilters_Response200() throws Exception {
        // given
        List<VisitDto> visits = List.of(
                new VisitDto(1L, START_TIME, END_TIME, buildDoctorDto(), buildPatientDto()),
                new VisitDto(2L, START_TIME.plusMinutes(30), END_TIME.plusMinutes(30), buildDoctorDto(), null)
        );
        when(visitService.getVisitsByCriteria(isNull(), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(visits));
        // when & then
        mockMvc.perform(get("/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].startTime").value("2099-12-01T09:00:00"))
                .andExpect(jsonPath("$.content[0].endTime").value("2099-12-01T09:30:00"))
                .andExpect(jsonPath("$.content[0].doctor.id").value(1))
                .andExpect(jsonPath("$.content[0].doctor.user.email").value("jan@test.pl"))
                .andExpect(jsonPath("$.content[0].doctor.specialization").value("CARDIOLOGIST"))
                .andExpect(jsonPath("$.content[0].patient.id").value(1))
                .andExpect(jsonPath("$.content[0].patient.user.email").value("anna@test.pl"))
                .andExpect(jsonPath("$.content[0].patient.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].startTime").value("2099-12-01T09:30:00"))
                .andExpect(jsonPath("$.content[1].patient").doesNotExist());
        verify(visitService).getVisitsByCriteria(isNull(), eq(false), any(Pageable.class));
        verifyNoMoreInteractions(visitService);
    }

    @Test
    void getVisits_FilterByPatientId_Response200() throws Exception {
        // given
        Long patientId = 1L;
        List<VisitDto> visits = List.of(
                new VisitDto(1L, START_TIME, END_TIME, buildDoctorDto(), buildPatientDto())
        );
        when(visitService.getVisitsByCriteria(eq(patientId), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(visits));
        // when & then
        mockMvc.perform(get("/visits").param("patientId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].patient.id").value(1))
                .andExpect(jsonPath("$.content[0].patient.user.email").value("anna@test.pl"));
        verify(visitService).getVisitsByCriteria(eq(patientId), eq(false), any(Pageable.class));
        verifyNoMoreInteractions(visitService);
    }

    @Test
    void getVisits_OnlyAvailable_Response200() throws Exception {
        // given
        List<VisitDto> visits = List.of(
                new VisitDto(2L, START_TIME, END_TIME, buildDoctorDto(), null)
        );
        when(visitService.getVisitsByCriteria(isNull(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(visits));
        // when & then
        mockMvc.perform(get("/visits").param("onlyAvailable", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].doctor.id").value(1))
                .andExpect(jsonPath("$.content[0].patient").doesNotExist());
        verify(visitService).getVisitsByCriteria(isNull(), eq(true), any(Pageable.class));
        verifyNoMoreInteractions(visitService);
    }

    @Test
    void getVisits_PatientIdAndOnlyAvailable_Response400() throws Exception {
        // given
        when(visitService.getVisitsByCriteria(eq(1L), eq(true), any(Pageable.class)))
                .thenThrow(new InvalidVisitFilterException());
        // when & then
        mockMvc.perform(get("/visits")
                        .param("patientId", "1")
                        .param("onlyAvailable", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cannot filter by patientId and onlyAvailable=true"))
                .andExpect(jsonPath("$.path").value("/visits"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addVisit_DataCorrect_Response201() throws Exception {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, START_TIME, END_TIME);
        VisitDto visitDto = new VisitDto(1L, START_TIME, END_TIME, buildDoctorDto(), null);
        when(visitService.addVisit(command)).thenReturn(visitDto);
        // when & then
        mockMvc.perform(post("/visits")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startTime").value("2099-12-01T09:00:00"))
                .andExpect(jsonPath("$.endTime").value("2099-12-01T09:30:00"))
                .andExpect(jsonPath("$.doctor.id").value(1))
                .andExpect(jsonPath("$.doctor.user.email").value("jan@test.pl"))
                .andExpect(jsonPath("$.doctor.specialization").value("CARDIOLOGIST"))
                .andExpect(jsonPath("$.patient").doesNotExist());
        verify(visitService).addVisit(command);
        verifyNoMoreInteractions(visitService);
    }

    @Test
    void addVisit_DoctorNotFound_Response404() throws Exception {
        // given
        CreateVisitCommand command = new CreateVisitCommand(99L, START_TIME, END_TIME);
        when(visitService.addVisit(command))
                .thenThrow(new DoctorNotFoundException("The doctor with the provided id does not exist"));
        // when & then
        mockMvc.perform(post("/visits")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The doctor with the provided id does not exist"))
                .andExpect(jsonPath("$.path").value("/visits"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addVisit_VisitOverlap_Response409() throws Exception {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, START_TIME, END_TIME);
        when(visitService.addVisit(command))
                .thenThrow(new VisitOverlapException("The doctor already has a scheduled visit"));
        // when & then
        mockMvc.perform(post("/visits")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("The doctor already has a scheduled visit"))
                .andExpect(jsonPath("$.path").value("/visits"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addVisit_InvalidVisitTime_Response400() throws Exception {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L, START_TIME.plusMinutes(10), END_TIME);
        when(visitService.addVisit(command))
                .thenThrow(new InvalidVisitTimeException("Time must be in full quarter-hour"));
        // when & then
        mockMvc.perform(post("/visits")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Time must be in full quarter-hour"))
                .andExpect(jsonPath("$.path").value("/visits"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addVisit_TimesInPast_Response400() throws Exception {
        // given
        CreateVisitCommand command = new CreateVisitCommand(1L,
                LocalDateTime.of(2000, 1, 1, 9, 0), LocalDateTime.of(2000, 1, 1, 9, 30));
        // when & then
        mockMvc.perform(post("/visits")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.startTime").exists())
                .andExpect(jsonPath("$.fieldErrors.endTime").exists());
        verifyNoInteractions(visitService);
    }

    @Test
    void bookVisit_DataCorrect_Response200() throws Exception {
        // given
        Long visitId = 1L;
        BookVisitCommand command = new BookVisitCommand(1L);
        VisitDto visitDto = new VisitDto(1L, START_TIME, END_TIME, buildDoctorDto(), buildPatientDto());
        when(visitService.bookVisit(visitId, command.patientId())).thenReturn(visitDto);
        // when & then
        mockMvc.perform(post("/visits/{id}/book", visitId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startTime").value("2099-12-01T09:00:00"))
                .andExpect(jsonPath("$.endTime").value("2099-12-01T09:30:00"))
                .andExpect(jsonPath("$.doctor.id").value(1))
                .andExpect(jsonPath("$.patient.id").value(1))
                .andExpect(jsonPath("$.patient.user.email").value("anna@test.pl"))
                .andExpect(jsonPath("$.patient.user.firstName").value("Anna"))
                .andExpect(jsonPath("$.patient.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.patient.birthday").value("1990-05-12"));
        verify(visitService).bookVisit(visitId, command.patientId());
        verifyNoMoreInteractions(visitService);
    }

    @Test
    void bookVisit_VisitNotFound_Response404() throws Exception {
        // given
        Long visitId = 99L;
        BookVisitCommand command = new BookVisitCommand(1L);
        when(visitService.bookVisit(visitId, command.patientId()))
                .thenThrow(new VisitNotFoundException("The visit with the provided id does not exist"));
        // when & then
        mockMvc.perform(post("/visits/{id}/book", visitId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The visit with the provided id does not exist"))
                .andExpect(jsonPath("$.path").value("/visits/99/book"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void bookVisit_PatientNotFound_Response404() throws Exception {
        // given
        Long visitId = 1L;
        BookVisitCommand command = new BookVisitCommand(99L);
        when(visitService.bookVisit(visitId, command.patientId()))
                .thenThrow(new PatientNotFoundException("The patient with the provided email address does not exist"));
        // when & then
        mockMvc.perform(post("/visits/{id}/book", visitId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The patient with the provided email address does not exist"))
                .andExpect(jsonPath("$.path").value("/visits/1/book"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void bookVisit_VisitAlreadyBooked_Response409() throws Exception {
        // given
        Long visitId = 1L;
        BookVisitCommand command = new BookVisitCommand(1L);
        when(visitService.bookVisit(visitId, command.patientId()))
                .thenThrow(new VisitAlreadyBookedException("This visit is already booked"));
        // when & then
        mockMvc.perform(post("/visits/{id}/book", visitId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("This visit is already booked"))
                .andExpect(jsonPath("$.path").value("/visits/1/book"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void bookVisit_VisitInPast_Response400() throws Exception {
        // given
        Long visitId = 1L;
        BookVisitCommand command = new BookVisitCommand(1L);
        when(visitService.bookVisit(visitId, command.patientId()))
                .thenThrow(new InvalidVisitTimeException("Cannot create or book a visit in the past"));
        // when & then
        mockMvc.perform(post("/visits/{id}/book", visitId)
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cannot create or book a visit in the past"))
                .andExpect(jsonPath("$.path").value("/visits/1/book"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}