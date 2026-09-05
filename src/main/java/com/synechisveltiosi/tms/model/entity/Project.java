package com.synechisveltiosi.tms.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "project")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project implements Serializable {

    @Id
    @UuidGenerator
    private UUID id;

    @Version
    private long version;

    @Column(nullable = false, unique = true, length = 150)
    private String name;
    @Column(length = 1_000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToMany
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    /** Idempotently assigns the project while keeping both sides of the association consistent. */
    public void assignTo(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee must not be null");
        }
        boolean alreadyAssigned = employees.stream().map(Employee::getId).anyMatch(employee.getId()::equals);
        if (!alreadyAssigned) {
            employees.add(employee);
        }
        boolean projectMissingFromEmployee = employee.getProjects().stream()
                .map(Project::getId)
                .noneMatch(projectId -> java.util.Objects.equals(projectId, id));
        if (projectMissingFromEmployee) {
            employee.getProjects().add(this);
        }
    }

    public void addEmployee(Employee employee) {
        assignTo(employee);
    }

    public void changeActive(boolean active) {
        this.active = active;
    }

}
