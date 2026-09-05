package com.synechisveltiosi.tms.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "task", uniqueConstraints = @UniqueConstraint(
        name = "uk_task_project_name", columnNames = {"project_id", "name"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(nullable = false, length = 150)
    private String name;
    @Column(length = 1_000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToMany
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @OrderBy("date ASC")
    @Builder.Default
    private List<TimesheetEntry> timesheetEntries = new java.util.ArrayList<>();

    public void addTimesheetEntry(TimesheetEntry timesheetEntry) {
        timesheetEntries.add(timesheetEntry);
        timesheetEntry.setTask(this);
    }

    public void changeActive(boolean active) {
        this.active = active;
    }

    /** Idempotently assigns the task while keeping both sides of the association consistent. */
    public void assignTo(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee must not be null");
        }
        boolean alreadyAssigned = employees.stream()
                .map(Employee::getId)
                .anyMatch(employee.getId()::equals);
        if (!alreadyAssigned) {
            employees.add(employee);
        }
        boolean taskMissingFromEmployee = employee.getTasks().stream()
                .map(Task::getId)
                .noneMatch(taskId -> Objects.equals(taskId, id));
        if (taskMissingFromEmployee) {
            employee.getTasks().add(this);
        }
    }
}
