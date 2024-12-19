package com.primefactorsolutions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HoursWorked extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    private int weekNumber;
    private LocalDate date;
    private String actividad;
    private double hours;
    private double horaspendientes;
    private double totalHours;

    private String tareaEspecifica;

    public String getTareaEspecifica() {
        return tareaEspecifica;
    }

    public void setTareaEspecifica(final String tareaEspecifica) {
        this.tareaEspecifica = tareaEspecifica;
    }


    public static double calculateTotalHours(final List<HoursWorked> activities) {
        return activities.stream()
                .mapToDouble(activity -> activity.hours)
                .sum();
    }

    public static double calculatePendingHours(final List<HoursWorked> activities) {
        double totalHoursWorked = calculateTotalHours(activities);
        return Math.max(0, 40 - totalHoursWorked);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(final Employee employee) {
        this.employee = employee;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(final int weekNumber) {
        this.weekNumber = weekNumber;
    }
    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        this.date = date;
        if (date != null) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            this.weekNumber = date.get(weekFields.weekOfWeekBasedYear());
        }
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(final String actividad) {
        this.actividad = actividad;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(final double hours) {
        this.hours = hours;
    }

    public double getTotalHours() {
        double total = this.getHours();
        return totalHours + total;
    }

    public void setTotalHours(final double totalHours) {
        this.totalHours = totalHours;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(final Team team) {
        this.team = team;
    }

    public double getHoraspendientes() {
        //double horasTrabajadas = this.getTotalHours() + this.getHorasTareasEspecificas();
        return 40;
    }

    public void setHoraspendientes(final double horaspendientes) {
        this.horaspendientes = horaspendientes;
    }

}
