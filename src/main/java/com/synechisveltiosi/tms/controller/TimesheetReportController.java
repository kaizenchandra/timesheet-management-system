package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.response.TimesheetHoursSummaryDto;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.service.TimesheetReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(URLConstants.TimesheetReportEndpoint.BASE)
@RequiredArgsConstructor
public class TimesheetReportController {
    private final TimesheetReportService timesheetReportService;

    @GetMapping("/employee/{employeeId}/hours")
    public ResponseEntity<TimesheetHoursSummaryDto> summarizeHours(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "APPROVED") TimesheetStatus status) {
        return ResponseEntity.ok(timesheetReportService.summarizeHours(employeeId, startDate, endDate, status));
    }
}
