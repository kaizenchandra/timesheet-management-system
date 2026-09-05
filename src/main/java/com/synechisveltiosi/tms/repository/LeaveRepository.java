package com.synechisveltiosi.tms.repository;

import com.synechisveltiosi.tms.model.entity.Leave;
import com.synechisveltiosi.tms.model.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveRepository extends JpaRepository<Leave, UUID> {
    boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID employeeId, Collection<LeaveStatus> statuses, LocalDate endDate, LocalDate startDate);

    List<Leave> findAllByEmployeeIdOrderByStartDateDesc(UUID employeeId);

    List<Leave> findAllByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID employeeId, LeaveStatus status, LocalDate endDate, LocalDate startDate);

    Optional<Leave> findByIdAndEmployeeId(UUID id, UUID employeeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select leaveRequest from Leave leaveRequest where leaveRequest.id = :leaveId")
    Optional<Leave> findByIdForUpdate(@Param("leaveId") UUID leaveId);
}
