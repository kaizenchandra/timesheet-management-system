package com.synechisveltiosi.tms.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "holiday", uniqueConstraints = @UniqueConstraint(name = "uk_holiday_date", columnNames = "date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday implements Serializable {
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
    private LocalDate date;
}
