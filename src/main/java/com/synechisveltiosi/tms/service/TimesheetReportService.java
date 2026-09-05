package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;
import com.synechisveltiosi.tms.api.exception.ResourceValidationException;
import com.synechisveltiosi.tms.api.response.TimesheetHoursSummaryDto;
import com.synechisveltiosi.tms.model.enums.TimesheetEntryType;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import com.synechisveltiosi.tms.repository.TimesheetEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimesheetReportService {
    private static final long MAX_REPORTING_RANGE_DAYS = 366;

    private final TimesheetEntryRepository timesheetEntryRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public TimesheetHoursSummaryDto summarizeHours(UUID employeeId, LocalDate startDate,
                                                    LocalDate endDate, TimesheetStatus status) {
        validateRange(startDate, endDate);
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        return timesheetEntryRepository.summarizeHours(employeeId, status, startDate, endDate,
                TimesheetEntryType.BILLABLE, TimesheetEntryType.NON_BILLABLE, TimesheetEntryType.OVERTIME);
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResourceValidationException("Report start date cannot be after end date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > MAX_REPORTING_RANGE_DAYS) {
            throw new ResourceValidationException("Report date range cannot exceed 366 days");
        }
    }
}
