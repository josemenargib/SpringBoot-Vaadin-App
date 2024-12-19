package com.primefactorsolutions.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Timesheet extends BaseEntity {
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<TimesheetEntry> entries;
}
