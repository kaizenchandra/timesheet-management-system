package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.embed.PersonDetails;
import com.synechisveltiosi.tms.model.entity.Employee;

import java.util.UUID;

/** Compact employee hierarchy model for manager assignment and direct-report views. */
public record EmployeeHierarchyDto(UUID id, long version, UUID managerId, String name) {
    public EmployeeHierarchyDto(Employee employee) {
        this(employee.getId(), employee.getVersion(), managerId(employee), displayName(employee.getPersonDetails()));
    }

    private static UUID managerId(Employee employee) {
        return employee.getManager() == null ? null : employee.getManager().getId();
    }

    private static String displayName(PersonDetails personDetails) {
        if (personDetails == null || personDetails.getName() == null) {
            return null;
        }
        String firstName = personDetails.getName().getFirstName();
        String lastName = personDetails.getName().getLastName();
        return firstName == null ? lastName : lastName == null ? firstName : firstName + " " + lastName;
    }
}
