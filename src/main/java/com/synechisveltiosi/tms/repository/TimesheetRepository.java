package com.synechisveltiosi.tms.repository;

import com.synechisveltiosi.tms.model.entity.Timesheet;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {
    List<Timesheet> findAllByEmployeeIdOrderByStartDateDesc(UUID employeeId);

    @EntityGraph(attributePaths = "employee")
    Page<Timesheet> findByEmployeeManagerIdAndStatus(UUID managerId, TimesheetStatus status, Pageable pageable);

    Optional<Timesheet> findByIdAndEmployeeId(UUID id, UUID employeeId);

    @EntityGraph(attributePaths = {"employee", "entries", "entries.task"})
    Optional<Timesheet> findWithEntriesByIdAndEmployeeId(UUID id, UUID employeeId);

    Optional<Timesheet> findByIdAndEmployeeIdAndStatus(UUID id, UUID employeeId, TimesheetStatus status);

    /**
     * @deprecated use {@link #findAllByEmployeeIdOrderByStartDateDesc(UUID)}. Kept temporarily for API compatibility.
     */
    @Deprecated(forRemoval = false)
    @Query("select t from Timesheet t where t.employee.id = :employeeId")
    Optional<List<Timesheet>> findByEmployeeId(UUID employeeId);

    boolean existsByEmployeeIdAndStartDateAndEndDate(UUID employeeId, java.time.LocalDate startDate,
                                                      java.time.LocalDate endDate);

}
