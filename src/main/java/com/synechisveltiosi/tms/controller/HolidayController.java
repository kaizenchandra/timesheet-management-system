package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.HolidayRequest;
import com.synechisveltiosi.tms.api.response.HolidayDto;
import com.synechisveltiosi.tms.service.HolidayService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(URLConstants.HolidayEndpoint.BASE)
@RequiredArgsConstructor
@Validated
public class HolidayController {
    private final HolidayService holidayService;

    @PostMapping
    public ResponseEntity<HolidayDto> createHoliday(@Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holidayService.createHoliday(request));
    }

    @GetMapping
    public ResponseEntity<List<HolidayDto>> getHolidays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(holidayService.getHolidays(startDate, endDate));
    }

    @DeleteMapping("/{holidayId}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long holidayId,
                                               @RequestParam @PositiveOrZero long version) {
        holidayService.deleteHoliday(holidayId, version);
        return ResponseEntity.noContent().build();
    }
}
