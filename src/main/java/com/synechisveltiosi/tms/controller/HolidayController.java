package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.HolidayRequest;
import com.synechisveltiosi.tms.api.response.HolidayDto;
import com.synechisveltiosi.tms.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_INVALID_DATA;
import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_NOT_FOUND;

@RestController
@RequestMapping(URLConstants.HolidayEndpoint.BASE)
@RequiredArgsConstructor
@Validated
@Tag(name = "Holiday", description = "Holiday management APIs")
public class HolidayController {
    private final HolidayService holidayService;

    /**
     * Creates a new holiday entry.
     */
    @Operation(
            summary = "Create holiday",
            description = "Creates a holiday record with the provided details."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Holiday created successfully",
            content = @Content(schema = @Schema(implementation = HolidayDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @PostMapping
    public ResponseEntity<HolidayDto> createHoliday(@Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holidayService.createHoliday(request));
    }

    /**
     * Retrieves holidays within a date range.
     */
    @Operation(
            summary = "Get holidays by date range",
            description = "Returns all holidays scheduled between the provided start and end dates."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Holidays retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HolidayDto.class)))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @GetMapping
    public ResponseEntity<List<HolidayDto>> getHolidays(
            @RequestParam("startDate")
            @Parameter(description = "Start date of the holiday lookup range (ISO-8601)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")
            @Parameter(description = "End date of the holiday lookup range (ISO-8601)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(holidayService.getHolidays(startDate, endDate));
    }

    /**
     * Deletes an existing holiday entry by id.
     */
    @Operation(
            summary = "Delete holiday",
            description = "Deletes a holiday by its identifier, using the optimistic locking version to prevent stale deletes."
    )
    @ApiResponse(responseCode = "204", description = "Holiday deleted successfully")
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @DeleteMapping("/{holidayId}")
    public ResponseEntity<Void> deleteHoliday(
            @PathVariable("holidayId")
            @Parameter(description = "Unique identifier of the holiday to delete", required = true)
            Long holidayId,
            @RequestParam("version")
            @Parameter(description = "Optimistic locking version for the holiday record", required = true)
            @PositiveOrZero long version) {
        holidayService.deleteHoliday(holidayId, version);
        return ResponseEntity.noContent().build();
    }
}
