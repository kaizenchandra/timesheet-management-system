package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.embed.PersonDetails;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Timesheet;

import java.time.LocalDate;
import java.util.UUID;

/** Compact manager work-queue representation; it intentionally excludes entries and approval history. */
public record PendingTimesheetDto(
        UUID id,
        long version,
        UUID employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate) {

    public PendingTimesheetDto(Timesheet timesheet) {
        this(timesheet.getId(), timesheet.getVersion(), timesheet.getEmployee().getId(),
                fullName(timesheet.getEmployee()), timesheet.getStartDate(), timesheet.getEndDate());
    }

    private static String fullName(Employee employee) {
        PersonDetails details = employee.getPersonDetails();
        if (details == null || details.getName() == null) {
            return null;
        }
        String firstName = details.getName().getFirstName();
        String lastName = details.getName().getLastName();
        if (firstName == null) {
            return lastName;
        }
        return lastName == null ? firstName : firstName + " " + lastName;
    }
}
