package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.response.TimesheetHoursSummaryDto;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.service.TimesheetReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_INVALID_DATA;
import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_NOT_FOUND;

@RestController
@RequestMapping(URLConstants.TimesheetReportEndpoint.BASE)
@RequiredArgsConstructor
@Tag(name = "Timesheet Report", description = "Timesheet reporting and summary APIs")
public class TimesheetReportController {
    private final TimesheetReportService timesheetReportService;

    /**
     * Produces a filtered hours summary for an employee over a date range.
     */
    @Operation(
            summary = "Summarize employee hours",
            description = "Returns the aggregated hours summary for an employee within a date range and optional timesheet status filter."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Summary generated successfully",
            content = @Content(schema = @Schema(implementation = TimesheetHoursSummaryDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @GetMapping("/employee/{employeeId}/hours")
    public ResponseEntity<TimesheetHoursSummaryDto> summarizeHours(
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee whose hours summary is requested", required = true)
            UUID employeeId,
            @RequestParam("startDate")
            @Parameter(description = "Start date of the summary range (ISO-8601)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")
            @Parameter(description = "End date of the summary range (ISO-8601)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", defaultValue = "APPROVED")
            @Parameter(description = "Timesheet status filter used for the summary", required = false)
            TimesheetStatus status) {
        return ResponseEntity.ok(timesheetReportService.summarizeHours(employeeId, startDate, endDate, status));
    }
}
