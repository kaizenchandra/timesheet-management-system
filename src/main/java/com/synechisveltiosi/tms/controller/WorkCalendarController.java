package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.response.WorkCalendarDayDto;
import com.synechisveltiosi.tms.service.WorkCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.util.List;
import java.util.UUID;

import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_INVALID_DATA;
import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_NOT_FOUND;

@RestController
@RequestMapping(URLConstants.WorkCalendarEndpoint.BASE)
@RequiredArgsConstructor
@Tag(name = "Work Calendar", description = "Work calendar query APIs")
public class WorkCalendarController {
    private final WorkCalendarService workCalendarService;

    /**
     * Returns the work calendar entries for an employee within a date range.
     */
    @Operation(
            summary = "Get employee work calendar",
            description = "Returns the daily work calendar details for an employee across the requested date range."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Work calendar retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkCalendarDayDto.class)))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<WorkCalendarDayDto>> getCalendar(
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee whose calendar is requested", required = true)
            UUID employeeId,
            @RequestParam("startDate")
            @Parameter(description = "Start date of the calendar range (ISO-8601)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")
            @Parameter(description = "End date of the calendar range (ISO-8601)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(workCalendarService.getCalendar(employeeId, startDate, endDate));
    }
}
