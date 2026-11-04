package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.BookVisitCommand;
import com.radom07.medicalclinic.command.CreateVisitCommand;
import com.radom07.medicalclinic.model.dto.VisitDto;
import com.radom07.medicalclinic.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
@Validated
public class VisitController {

    private final VisitService visitService;

    @GetMapping("/available")
    public List<VisitDto> getAvailableVisits() {
        return visitService.getAvailableVisits();
    }

    @GetMapping("/patient/{email}")
    public List<VisitDto> getPatientVisits(@PathVariable String email) {
        return visitService.getPatientVisits(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto addVisit(@Valid @RequestBody CreateVisitCommand command) {
        return visitService.addVisit(command);
    }

    @PatchMapping("/{id}/book")
    public VisitDto bookVisit(@PathVariable Long id, @Valid @RequestBody BookVisitCommand command) {
        return visitService.bookVisit(id, command.patientEmail());
    }
}
