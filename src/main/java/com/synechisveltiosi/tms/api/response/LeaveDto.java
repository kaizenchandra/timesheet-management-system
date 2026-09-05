package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.enums.LeaveStatus;
import com.synechisveltiosi.tms.model.enums.LeaveType;
import com.synechisveltiosi.tms.model.entity.Leave;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Leave}
 */
public record LeaveDto(UUID id, long version, LocalDate startDate, LocalDate endDate, LeaveType type,
                       LeaveStatus status, String reason, double hours, String comments,
                       List<LeaveApprovalDto> approvals) implements Serializable {
    public LeaveDto(Leave leave) {
        this(leave.getId(), leave.getVersion(), leave.getStartDate(), leave.getEndDate(), leave.getType(),
                leave.getStatus(), leave.getReason(), leave.getHours(), leave.getComments(),
                leave.getApprovals().stream().map(LeaveApprovalDto::new).toList());
    }
}
