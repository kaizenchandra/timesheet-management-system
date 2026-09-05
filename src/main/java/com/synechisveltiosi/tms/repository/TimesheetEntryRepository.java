package com.synechisveltiosi.tms.repository;

import com.synechisveltiosi.tms.api.response.TimesheetHoursSummaryDto;
import com.synechisveltiosi.tms.model.entity.TimesheetEntry;
import com.synechisveltiosi.tms.model.enums.TimesheetEntryType;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, UUID> {
    @Query("""
            select new com.synechisveltiosi.tms.api.response.TimesheetHoursSummaryDto(
                :startDate, :endDate,
                coalesce(sum(entry.hours), 0.0),
                coalesce(sum(case when entry.entryType = :billableType then entry.hours else 0.0 end), 0.0),
                coalesce(sum(case when entry.entryType = :nonBillableType then entry.hours else 0.0 end), 0.0),
                coalesce(sum(case when entry.entryType = :overtimeType then entry.hours else 0.0 end), 0.0),
                count(entry.id))
            from TimesheetEntry entry
            where entry.timesheet.employee.id = :employeeId
              and entry.timesheet.status = :status
              and entry.date between :startDate and :endDate
            """)
    TimesheetHoursSummaryDto summarizeHours(
            @Param("employeeId") UUID employeeId,
            @Param("status") TimesheetStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("billableType") TimesheetEntryType billableType,
            @Param("nonBillableType") TimesheetEntryType nonBillableType,
            @Param("overtimeType") TimesheetEntryType overtimeType);
}
