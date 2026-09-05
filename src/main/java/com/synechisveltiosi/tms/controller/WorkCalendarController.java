package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.response.WorkCalendarDayDto;
import com.synechisveltiosi.tms.service.WorkCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(URLConstants.WorkCalendarEndpoint.BASE)
@RequiredArgsConstructor
public class WorkCalendarController {
    private final WorkCalendarService workCalendarService;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<WorkCalendarDayDto>> getCalendar(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(workCalendarService.getCalendar(employeeId, startDate, endDate));
    }
}
