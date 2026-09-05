package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;
import com.synechisveltiosi.tms.api.exception.ResourceUpdateException;
import com.synechisveltiosi.tms.api.exception.ResourceValidationException;
import com.synechisveltiosi.tms.api.request.LeaveCancellationRequest;
import com.synechisveltiosi.tms.api.request.LeaveRequest;
import com.synechisveltiosi.tms.api.request.LeaveDecisionRequest;
import com.synechisveltiosi.tms.api.response.LeaveDto;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Leave;
import com.synechisveltiosi.tms.model.entity.LeaveApproval;
import com.synechisveltiosi.tms.model.enums.LeaveStatus;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import com.synechisveltiosi.tms.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private static final List<LeaveStatus> ACTIVE_STATUSES = List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED);

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveDto requestLeave(UUID employeeId, LeaveRequest request) {
        validateRequest(request);
        // Serializing requests per employee closes the application-level overlap race for this monolith.
        Employee employee = employeeRepository.findByIdForUpdate(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (leaveRepository.existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                employeeId, ACTIVE_STATUSES, request.endDate(), request.startDate())) {
            throw new ResourceValidationException("The requested leave period overlaps an active leave request");
        }

        Leave leave = Leave.builder()
                .employee(employee)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .type(request.type())
                .reason(request.reason().trim())
                .hours(request.hours())
                .status(LeaveStatus.PENDING)
                .build();
        return new LeaveDto(leaveRepository.saveAndFlush(leave));
    }

    @Transactional(readOnly = true)
    public List<LeaveDto> getLeavesForEmployee(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        return leaveRepository.findAllByEmployeeIdOrderByStartDateDesc(employeeId).stream()
                .map(LeaveDto::new)
                .toList();
    }

    @Transactional
    public LeaveDto cancelLeave(UUID leaveId, UUID employeeId, LeaveCancellationRequest request) {
        Leave leave = leaveRepository.findByIdAndEmployeeId(leaveId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found for the employee"));
        if (leave.getVersion() != request.version()) {
            throw new ResourceUpdateException("The leave request has changed. Refresh it before retrying your request");
        }
        if (leave.getStatus() != LeaveStatus.PENDING && leave.getStatus() != LeaveStatus.APPROVED) {
            throw new ResourceUpdateException("Only pending or approved leave can be cancelled");
        }
        leave.cancel(request.comments());
        return new LeaveDto(leaveRepository.saveAndFlush(leave));
    }

    @Transactional
    public LeaveDto decideLeave(UUID leaveId, UUID managerId, LeaveDecisionRequest request) {
        Leave leave = leaveRepository.findByIdForUpdate(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveId));
        if (leave.getVersion() != request.version()) {
            throw new ResourceUpdateException("The leave request has changed. Refresh it before retrying your request");
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ResourceUpdateException("Only pending leave can be approved or rejected");
        }
        if (request.status() != LeaveStatus.APPROVED && request.status() != LeaveStatus.REJECTED) {
            throw new ResourceUpdateException("Only APPROVED or REJECTED are valid leave decisions");
        }
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));
        if (leave.getEmployee().getManager() == null
                || !leave.getEmployee().getManager().getId().equals(manager.getId())) {
            throw new ResourceUpdateException("Only the employee's manager can decide this leave request");
        }

        leave.decide(request.status(), request.comments());
        leave.addApproval(LeaveApproval.builder()
                .approver(manager)
                .status(request.status())
                .comments(request.comments())
                .build());
        return new LeaveDto(leaveRepository.saveAndFlush(leave));
    }

    private void validateRequest(LeaveRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new ResourceValidationException("Leave start date cannot be after end date");
        }
        if (!Double.isFinite(request.hours()) || request.hours() < 0.25d) {
            throw new ResourceValidationException("Leave hours must be a finite value of at least 0.25");
        }
    }
}
