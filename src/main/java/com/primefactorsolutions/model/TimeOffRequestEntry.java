package com.primefactorsolutions.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TimeOffRequestEntry extends BaseEntity {
    private LocalDate date;
    @Nullable
    private PartOfDay partOfDay;
    @ManyToOne
    private TimeOffRequest request;
}
