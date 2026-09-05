package com.synechisveltiosi.tms.model.entity;

import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Clock;

@Entity
@Table(name = "timesheet_approval")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetApproval implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_date")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;

    private String comments;

    @ManyToOne
    @JoinColumn(name = "approver_id", nullable = false)
    private Employee approver;

    @ManyToOne
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;

    @PrePersist
    public void prePersist() {
        if (date == null) {
            date = LocalDate.now(Clock.systemUTC());
        }
        if (comments == null) {
            comments = "";
        }
    }
}
