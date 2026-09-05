package com.synechisveltiosi.tms.model.entity;

import com.synechisveltiosi.tms.model.enums.LeaveStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;

@Entity
@Table(name = "leave_approval")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    private String comments;

    private LocalDate decisionDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "approver_id", nullable = false)
    private Employee approver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "leave_id", nullable = false)
    private Leave leave;

    void setLeave(Leave leave) {
        this.leave = leave;
    }

    @PrePersist
    void initializeDecisionDate() {
        if (decisionDate == null) {
            decisionDate = LocalDate.now(Clock.systemUTC());
        }
        if (comments == null) {
            comments = "";
        }
    }
}
