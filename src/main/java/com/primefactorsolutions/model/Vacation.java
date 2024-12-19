package com.primefactorsolutions.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Vacation extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private TimeOffRequestType category;
    private Integer  monthOfYear;
    private Integer  dayOfMonth;
    private Double duration;
    private Double expiration;
    @Enumerated(EnumType.STRING)
    private Type type;
    public enum Type {
        FIXED,
        MOVABLE,
        OTHER
    }
}
