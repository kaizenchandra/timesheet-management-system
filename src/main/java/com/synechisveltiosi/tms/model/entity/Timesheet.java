package com.synechisveltiosi.tms.model.entity;

import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "timesheet", uniqueConstraints = @UniqueConstraint(
        name = "uk_timesheet_employee_period", columnNames = {"employee_id", "start_date", "end_date"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timesheet implements Serializable {

    @Id
    @UuidGenerator
    private UUID id;

    @Version
    private long version;

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC")
    @Builder.Default
    private Collection<TimesheetEntry> entries = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC")
    @Builder.Default
    private Collection<TimesheetApproval> approvals = new ArrayList<>();

    public void addEntry(TimesheetEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Timesheet entry must not be null");
        }
        entries.add(entry);
        entry.setTimesheet(this);
    }

    public void addApproval(TimesheetApproval approval) {
        if (approval == null) {
            throw new IllegalArgumentException("Timesheet approval must not be null");
        }
        approvals.add(approval);
        approval.setTimesheet(this);
    }

    public void replaceEntries(Collection<TimesheetEntry> replacementEntries) {
        if (replacementEntries == null || replacementEntries.isEmpty()) {
            throw new IllegalArgumentException("Timesheet must contain at least one entry");
        }
        entries.clear();
        replacementEntries.forEach(this::addEntry);
    }

    public static Timesheet newDraft(Employee employee, LocalDate startDate, LocalDate endDate) {
        return Timesheet.builder()
                .employee(employee)
                .startDate(startDate)
                .endDate(endDate)
                .status(TimesheetStatus.DRAFTED)
                .build();
    }

    public void withdraw() {
        if (status != TimesheetStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted timesheets can be withdrawn");
        }
        status = TimesheetStatus.CANCELLED;
    }

}
