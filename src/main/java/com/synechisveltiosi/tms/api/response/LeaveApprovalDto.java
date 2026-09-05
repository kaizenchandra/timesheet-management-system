package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.entity.LeaveApproval;
import com.synechisveltiosi.tms.model.enums.LeaveStatus;

import java.time.LocalDate;
import java.util.UUID;

public record LeaveApprovalDto(Long id, UUID approverId, LocalDate decisionDate,
                               LeaveStatus status, String comments) {
    public LeaveApprovalDto(LeaveApproval approval) {
        this(approval.getId(), approval.getApprover().getId(), approval.getDecisionDate(),
                approval.getStatus(), approval.getComments());
    }
}
