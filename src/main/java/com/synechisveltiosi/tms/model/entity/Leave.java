package com.synechisveltiosi.tms.model.entity;


import com.synechisveltiosi.tms.model.enums.LeaveStatus;
import com.synechisveltiosi.tms.model.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leave")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Leave implements Serializable {
    @Id
    @UuidGenerator
    private UUID id;

    @Version
    private long version;

    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    private String reason;
    private double hours;
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @OneToMany(mappedBy = "leave", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("decisionDate ASC")
    @Builder.Default
    private List<LeaveApproval> approvals = new ArrayList<>();

    public void decide(LeaveStatus decision, String decisionComments) {
        if (status != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave can be approved or rejected");
        }
        if (decision != LeaveStatus.APPROVED && decision != LeaveStatus.REJECTED) {
            throw new IllegalArgumentException("Only APPROVED or REJECTED are valid leave decisions");
        }
        status = decision;
        comments = decisionComments == null ? "" : decisionComments;
    }

    public void addApproval(LeaveApproval approval) {
        if (approval == null) {
            throw new IllegalArgumentException("Leave approval must not be null");
        }
        approvals.add(approval);
        approval.setLeave(this);
    }

    public void cancel(String cancellationComments) {
        if (status != LeaveStatus.PENDING && status != LeaveStatus.APPROVED) {
            throw new IllegalStateException("Only pending or approved leave can be cancelled");
        }
        status = LeaveStatus.CANCELLED;
        comments = cancellationComments == null ? "" : cancellationComments;
    }
}
