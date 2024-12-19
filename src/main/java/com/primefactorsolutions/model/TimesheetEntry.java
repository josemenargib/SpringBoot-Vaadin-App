package com.primefactorsolutions.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetEntry {
    private TaskType taskType;
    private int hours;
}
