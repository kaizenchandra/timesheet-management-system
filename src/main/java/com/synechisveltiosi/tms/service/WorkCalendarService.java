package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;
import com.synechisveltiosi.tms.api.exception.ResourceValidationException;
import com.synechisveltiosi.tms.api.response.WorkCalendarDayDto;
import com.synechisveltiosi.tms.model.entity.Holiday;
import com.synechisveltiosi.tms.model.entity.Leave;
import com.synechisveltiosi.tms.model.enums.LeaveStatus;
import com.synechisveltiosi.tms.model.enums.LeaveType;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import com.synechisveltiosi.tms.repository.HolidayRepository;
import com.synechisveltiosi.tms.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkCalendarService {
    private static final long MAX_CALENDAR_RANGE_DAYS = 366;

    private final EmployeeRepository employeeRepository;
    private final HolidayRepository holidayRepository;
    private final LeaveRepository leaveRepository;

    @Transactional(readOnly = true)
    public List<WorkCalendarDayDto> getCalendar(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        Map<LocalDate, String> holidays = holidayRepository.findAllByDateBetweenOrderByDateAsc(startDate, endDate)
                .stream().collect(java.util.stream.Collectors.toMap(Holiday::getDate, Holiday::getName,
                        (firstName, ignoredDuplicate) -> firstName));
        Map<LocalDate, LeaveType> approvedLeave = expandApprovedLeave(employeeId, startDate, endDate);

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> calendarDay(date, holidays.get(date), approvedLeave.get(date)))
                .toList();
    }

    private Map<LocalDate, LeaveType> expandApprovedLeave(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, LeaveType> leaveByDate = new HashMap<>();
        for (Leave leave : leaveRepository.findAllByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                employeeId, LeaveStatus.APPROVED, endDate, startDate)) {
            LocalDate effectiveStart = leave.getStartDate().isBefore(startDate) ? startDate : leave.getStartDate();
            LocalDate effectiveEnd = leave.getEndDate().isAfter(endDate) ? endDate : leave.getEndDate();
            effectiveStart.datesUntil(effectiveEnd.plusDays(1))
                    .forEach(date -> leaveByDate.put(date, leave.getType()));
        }
        return leaveByDate;
    }

    private WorkCalendarDayDto calendarDay(LocalDate date, String holidayName, LeaveType leaveType) {
        boolean weekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        return new WorkCalendarDayDto(date, weekend, holidayName, leaveType,
                !weekend && holidayName == null && leaveType == null);
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResourceValidationException("Calendar start date cannot be after end date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > MAX_CALENDAR_RANGE_DAYS) {
            throw new ResourceValidationException("Calendar date range cannot exceed 366 days");
        }
    }
}
