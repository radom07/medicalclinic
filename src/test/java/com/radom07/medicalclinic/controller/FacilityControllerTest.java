package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.CreateFacilityCommand;
import com.radom07.medicalclinic.exception.FacilityAlreadyExistsException;
import com.radom07.medicalclinic.exception.FacilityNotFoundException;
import com.radom07.medicalclinic.model.dto.FacilityDto;
import com.radom07.medicalclinic.service.FacilityService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
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

@WebMvcTest(FacilityController.class)
@AutoConfigureMockMvc
class FacilityControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FacilityService facilityService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getFacilities_DataCorrect_Response200() throws Exception {
        // given
        List<FacilityDto> facilities = List.of(
                new FacilityDto(1L, "Centrum Medyczne Warszawa", "Warszawa", "00-001", "Marszałkowska", "10A",
                        "221234567", Set.of("CARDIOLOGIST")),
                new FacilityDto(2L, "Przychodnia Radom", "Radom", "26-600", "Żeromskiego", "5",
                        "483334444", Set.of())
        );
        when(facilityService.getFacilities(any(Pageable.class))).thenReturn(new PageImpl<>(facilities));
        // when & then
        mockMvc.perform(get("/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Centrum Medyczne Warszawa"))
                .andExpect(jsonPath("$.content[0].city").value("Warszawa"))
                .andExpect(jsonPath("$.content[0].postalCode").value("00-001"))
                .andExpect(jsonPath("$.content[0].street").value("Marszałkowska"))
                .andExpect(jsonPath("$.content[0].buildingNo").value("10A"))
                .andExpect(jsonPath("$.content[0].phoneNumber").value("221234567"))
                .andExpect(jsonPath("$.content[0].doctorsSpecialization.length()").value(1))
                .andExpect(jsonPath("$.content[0].doctorsSpecialization[0]").value("CARDIOLOGIST"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Przychodnia Radom"))
                .andExpect(jsonPath("$.content[1].city").value("Radom"))
                .andExpect(jsonPath("$.content[1].doctorsSpecialization.length()").value(0));
        verify(facilityService).getFacilities(any(Pageable.class));
        verifyNoMoreInteractions(facilityService);
    }

    @Test
    void getFacilityById_DataCorrect_Response200() throws Exception {
        // given
        Long facilityId = 1L;
        FacilityDto facilityDto = new FacilityDto(1L, "Centrum Medyczne Warszawa", "Warszawa", "00-001",
                "Marszałkowska", "10A", "221234567", Set.of("SURGEON"));
        when(facilityService.getFacilityById(facilityId)).thenReturn(facilityDto);
        // when & then
        mockMvc.perform(get("/facilities/{id}", facilityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Centrum Medyczne Warszawa"))
                .andExpect(jsonPath("$.city").value("Warszawa"))
                .andExpect(jsonPath("$.postalCode").value("00-001"))
                .andExpect(jsonPath("$.street").value("Marszałkowska"))
                .andExpect(jsonPath("$.buildingNo").value("10A"))
                .andExpect(jsonPath("$.phoneNumber").value("221234567"))
                .andExpect(jsonPath("$.doctorsSpecialization[0]").value("SURGEON"));
        verify(facilityService).getFacilityById(facilityId);
        verifyNoMoreInteractions(facilityService);
    }

    @Test
    void getFacilityById_FacilityNotFound_Response404() throws Exception {
        // given
        Long facilityId = 1L;
        when(facilityService.getFacilityById(facilityId))
                .thenThrow(new FacilityNotFoundException("The medical facility with the provided id does not exist"));
        // when & then
        mockMvc.perform(get("/facilities/{id}", facilityId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The medical facility with the provided id does not exist"))
                .andExpect(jsonPath("$.path").value("/facilities/1"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addFacility_DataCorrect_Response201() throws Exception {
        // given
        CreateFacilityCommand command = new CreateFacilityCommand("Centrum Medyczne Warszawa", "Warszawa", "00-001",
                "Marszałkowska", "10A", "221234567");
        FacilityDto facilityDto = new FacilityDto(1L, "Centrum Medyczne Warszawa", "Warszawa", "00-001",
                "Marszałkowska", "10A", "221234567", Set.of());
        when(facilityService.addFacility(command)).thenReturn(facilityDto);
        // when & then
        mockMvc.perform(post("/facilities")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Centrum Medyczne Warszawa"))
                .andExpect(jsonPath("$.city").value("Warszawa"))
                .andExpect(jsonPath("$.postalCode").value("00-001"))
                .andExpect(jsonPath("$.street").value("Marszałkowska"))
                .andExpect(jsonPath("$.buildingNo").value("10A"))
                .andExpect(jsonPath("$.phoneNumber").value("221234567"))
                .andExpect(jsonPath("$.doctorsSpecialization.length()").value(0));
        verify(facilityService).addFacility(command);
        verifyNoMoreInteractions(facilityService);
    }

    @Test
    void addFacility_FacilityAlreadyExists_Response409() throws Exception {
        // given
        CreateFacilityCommand command = new CreateFacilityCommand("Centrum Medyczne Warszawa", "Warszawa", "00-001",
                "Marszałkowska", "10A", "221234567");
        when(facilityService.addFacility(command))
                .thenThrow(new FacilityAlreadyExistsException("The medical facility with the provided name already exists"));
        // when & then
        mockMvc.perform(post("/facilities")
                        .content(objectMapper.writeValueAsString(command))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("The medical facility with the provided name already exists"))
                .andExpect(jsonPath("$.path").value("/facilities"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deleteFacilityById_DataCorrect_Response204() throws Exception {
        // given
        Long facilityId = 1L;
        // when & then
        mockMvc.perform(delete("/facilities/{id}", facilityId))
                .andExpect(status().isNoContent());
        verify(facilityService).deleteFacilityById(facilityId);
        verifyNoMoreInteractions(facilityService);
    }

    @Test
    void deleteFacilityById_FacilityNotFound_Response404() throws Exception {
        // given
        Long facilityId = 1L;
        doThrow(new FacilityNotFoundException("The medical facility with the provided id does not exist"))
                .when(facilityService).deleteFacilityById(facilityId);
        // when & then
        mockMvc.perform(delete("/facilities/{id}", facilityId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The medical facility with the provided id does not exist"))
                .andExpect(jsonPath("$.path").value("/facilities/1"))
                .andExpect(jsonPath("$.timestamp").exists());
        verify(facilityService).deleteFacilityById(facilityId);
    }
}