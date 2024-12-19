package com.primefactorsolutions.views;

import com.primefactorsolutions.model.HoursWorked;
import com.primefactorsolutions.model.Team;
import com.primefactorsolutions.service.HoursWorkedService;
import com.primefactorsolutions.service.ReportService;
import com.primefactorsolutions.service.TeamService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import com.primefactorsolutions.service.EmployeeService;

import java.io.ByteArrayInputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@PermitAll
@Route(value = "/reportes", layout = MainLayout.class)
@PageTitle("Reporte de Horas Trabajadas")
public class ReporteView extends BaseView {

    private final EmployeeService employeeService;
    private final HoursWorkedService hoursWorkedService;
    private final ReportService reportService;
    private final TeamService teamService;

    private final ComboBox<Team> equipoComboBox = new ComboBox<>("Seleccionar Equipo");
    private final ComboBox<String> semanaComboBox = new ComboBox<>("Seleccionar Semana");
    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final VerticalLayout headerLayout = new VerticalLayout();
    private Anchor downloadLink;

    private final Span semanaInfoSpan = new Span();

    // Obtener el año actual
    private final int currentYear = LocalDate.now().getYear();

    @Autowired
    public ReporteView(final HoursWorkedService hoursWorkedService,
                       final ReportService reportService, final TeamService teamService,
                       final EmployeeService employeeService) {
        this.hoursWorkedService = hoursWorkedService;
        this.reportService = reportService;
        this.teamService = teamService;
        this.employeeService = employeeService;

        H2 title = new H2("Reporte de Horas Trabajadas");
        getCurrentPageLayout().add(title);

        List<Team> teams = teamService.findAllTeams();
        equipoComboBox.setItems(teams);
        equipoComboBox.setItemLabelGenerator(Team::getName);

        // Configurar el ComboBox de semanas
        initializeSemanaComboBox();

        // Listener para actualizar `semanaInfoSpan` con la selección del usuario en `semanaComboBox`
        semanaComboBox.addValueChangeListener(event -> {
            String selectedWeek = event.getValue();
            semanaInfoSpan.setText(selectedWeek != null ? selectedWeek : "Selecciona una semana");
        });

        Button reportButton = new Button("Generar Reporte de Horas Trabajadas",
                event -> generateHoursWorkedReport());
        getCurrentPageLayout().add(reportButton);

        HorizontalLayout filtersLayout = new HorizontalLayout(equipoComboBox, semanaComboBox);
        getCurrentPageLayout().add(filtersLayout);

        getCurrentPageLayout().add(headerLayout);
        updateHeaderLayout(null, null);

        grid.addColumn(map -> map.get("Empleado")).setHeader("Empleado");
        grid.addColumn(map -> map.get("Horas Trabajadas")).setHeader("Horas Trabajadas");
        grid.addColumn(map -> map.get("Horas Pendientes")).setHeader("Horas Pendientes");
        grid.addColumn(map -> map.get("Observaciones")).setHeader("Observaciones");

        getCurrentPageLayout().add(grid);
    }

    private void initializeSemanaComboBox() {
        int year = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(year, 1, 5);  // Suponemos que la semana comienza el 5 de enero.

        List<String> semanas = startOfYear.datesUntil(LocalDate.of(year + 1, 1, 1),
                        java.time.Period.ofWeeks(1))
                .map(date -> {
                    int weekNumber = date.get(WeekFields.of(DayOfWeek.MONDAY, 1)
                            .weekOfWeekBasedYear());
                    LocalDate endOfWeek = date.plusDays(6);

                    return String.format("Semana %d: %s - %s",
                            weekNumber,
                            date.getDayOfMonth() + " de " + date.getMonth()
                                    .getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            endOfWeek.getDayOfMonth() + " de " + endOfWeek.getMonth()
                                    .getDisplayName(TextStyle.FULL, Locale.getDefault())
                    );
                })
                .collect(Collectors.toList());

        semanaComboBox.setItems(semanas);
        semanaComboBox.setPlaceholder("Seleccione una semana");
    }

    private void generateHoursWorkedReport() {
        Team selectedEquipo = equipoComboBox.getValue();
        String selectedWeek = semanaComboBox.getValue();
        if (selectedEquipo == null || selectedWeek == null) {
            Notification.show("Por favor, selecciona un equipo y una semana para generar el reporte.",
                    3000, Notification.Position.MIDDLE);
            return;
        }

        int weekNumber = Integer.parseInt(selectedWeek.split(" ")[1].replace(":", ""));
        LocalDate selectedDate = LocalDate.now().with(WeekFields.of(DayOfWeek.FRIDAY, 1)
                .weekOfWeekBasedYear(), weekNumber);
        updateHeaderLayout(selectedEquipo, selectedDate);

        List<HoursWorked> hoursWorkedList = hoursWorkedService.findAll().stream()
                .filter(hw -> hw.getEmployee().getTeam().getId().equals(selectedEquipo
                        .getId()) && hw.getWeekNumber() == weekNumber)
                .collect(Collectors.toList());

        System.out.println(hoursWorkedList);
        if (hoursWorkedList.isEmpty()) {

            Notification.show("No hay horas trabajadas disponibles para generar el reporte.",
                    3000, Notification.Position.MIDDLE);
            return;
        }

        List<Map<String, Object>> data = hoursWorkedList.stream()
                .map(hoursWorked -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ID", hoursWorked.getId().toString());
                    map.put("Employee ID", hoursWorked.getEmployee().getId().toString());
                    map.put("Empleado", hoursWorked.getEmployee().getFirstName() + " "
                            + hoursWorked.getEmployee().getLastName());
                    map.put("Horas Trabajadas", hoursWorked.getTotalHours());
                    map.put("Horas Pendientes", 40 - hoursWorked.getTotalHours());
                    map.put("Observaciones", "");
                    return map;
                })
                .collect(Collectors.toList());

        grid.setItems(data);
        generateExcelDownloadLink(data, weekNumber);
    }

    private void updateHeaderLayout(final Team team, final LocalDate dateInWeek) {
        headerLayout.removeAll();

        if (team != null && dateInWeek != null) {
            int weekNumber = getWeekOfYear(dateInWeek);

            headerLayout.add(new Span("Informe "
                    + String.format("%03d", weekNumber) + "/" + currentYear) {{
                getStyle().set("font-size", "24px");
                getStyle().set("font-weight", "bold");
            }});

            String teamLeadName = employeeService.getTeamLeadName(team.getId());
            headerLayout.add(
                    new Span("Asunto: Informe Semanal de Horas Trabajadas") {{
                        getStyle().set("font-size", "18px");
                    }},
                    semanaInfoSpan,
                    new Span("Horas a cumplir: 40 horas") {{
                        getStyle().set("font-size", "18px");
                    }},
                    new Span("Equipo: " + team.getName()) {{
                        getStyle().set("font-size", "18px");
                    }},
                    new Span("Team Lead: " + teamLeadName) {{
                        getStyle().set("font-size", "18px");
                    }}
            );
        }
    }

    private void generateExcelDownloadLink(final List<Map<String, Object>> data, final int weekNumber) {
        try {
            List<String> headers = List.of("Empleado",
                    "Horas Trabajadas", "Horas Pendientes", "Observaciones");
            String selectedTeam = equipoComboBox.getValue().getName();
            byte[] excelBytes = reportService.writeAsExcel(
                    "hours_worked_report", headers, data, selectedTeam, weekNumber, currentYear);

            StreamResource excelResource = new StreamResource("hours_worked_report.xlsx",
                    () -> new ByteArrayInputStream(excelBytes));
            if (downloadLink == null) {
                downloadLink = new Anchor(excelResource, "Descargar Reporte en Excel");
                downloadLink.getElement().setAttribute("download", true);
                getCurrentPageLayout().add(downloadLink);
            } else {
                downloadLink.setHref(excelResource);
            }
        } catch (Exception e) {
            Notification.show("Error al generar el reporte de horas trabajadas en Excel.",
                    3000, Notification.Position.MIDDLE);
        }
    }

    private int getWeekOfYear(final LocalDate date) {
        return date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }

}
