package com.primefactorsolutions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TimeOffRequest extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private TimeOffRequestType category;
    @Enumerated(EnumType.STRING)
    private TimeOffRequestStatus state;
    private Double availableDays;
    private LocalDate expiration;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double daysToBeTake;
    private Double daysBalance;
}
