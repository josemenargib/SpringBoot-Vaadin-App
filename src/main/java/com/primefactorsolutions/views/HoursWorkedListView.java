package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Employee;
import com.primefactorsolutions.model.HoursWorked;
import com.primefactorsolutions.model.Team;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.service.HoursWorkedService;
import com.primefactorsolutions.service.TeamService;
import com.primefactorsolutions.views.util.MenuBarUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.components.grid.PagingGrid;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

import static com.primefactorsolutions.views.Constants.PAGE_SIZE;

@SpringComponent
@PermitAll
@Scope("prototype")
@PageTitle("Registro de Horas Trabajadas")
@Route(value = "/hours-worked-list", layout = MainLayout.class)
public class HoursWorkedListView extends BaseView {

    private final HoursWorkedService hoursWorkedService;
    private final EmployeeService employeeService;
    private final TeamService teamService;
    private final PagingGrid<HoursWorked> hoursWorkedGrid = new PagingGrid<>();
    private ComboBox<Employee> employeeFilter;
    private ComboBox<Team> teamFilter;

    public HoursWorkedListView(final HoursWorkedService hoursWorkedService,
                               final EmployeeService employeeService,
                               final TeamService teamService) {
        this.hoursWorkedService = hoursWorkedService;
        this.employeeService = employeeService;
        this.teamService = teamService;

        initializeView();
        refreshGridListHoursWorked(null, null);
    }

    private void refreshGridListHoursWorked(final Employee employee,
                                            final Team team) {
        hoursWorkedGrid.setPagingDataProvider((page, pageSize) -> {
            int start = (int) (page * hoursWorkedGrid.getPageSize());
            List<HoursWorked> hoursWorkedList = fetchFilteredHoursWorked(start, pageSize, employee, team);

            double totalHours = hoursWorkedList.stream()
                    .mapToDouble(HoursWorked::getTotalHours)
                    .sum();

            Notification.show("Total de horas trabajadas: " + totalHours,
                    3000, Notification.Position.BOTTOM_CENTER);

            return hoursWorkedList;
        });
        hoursWorkedGrid.getDataProvider().refreshAll();
    }

    private List<HoursWorked> fetchFilteredHoursWorked(final int start,
                                                       final int pageSize,
                                                       final Employee employee,
                                                       final Team team) {
        List<HoursWorked> filteredHoursWorked = hoursWorkedService.findAll();

        if (employee != null && !"TODOS".equals(employee.getFirstName())) {
            filteredHoursWorked = filteredHoursWorked.stream()
                    .filter(hw -> hw.getEmployee().getId().equals(employee.getId()))
                    .collect(Collectors.toList());
        }

        if (team != null && !"TODOS".equals(team.getName())) {
            filteredHoursWorked = filteredHoursWorked.stream()
                    .filter(hw -> hw.getEmployee().getTeam() != null
                            && hw.getEmployee().getTeam().getId().equals(team.getId()))
                    .collect(Collectors.toList());
        }

        for (HoursWorked hoursWorked : filteredHoursWorked) {
            if (employee != null && hoursWorked.getEmployee().getId().equals(employee.getId())) {
                LocalDate date = hoursWorked.getDate();
                int currentWeek = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

                double totalWorkedInSameWeek = filteredHoursWorked.stream()
                        .filter(hw -> hw.getEmployee().getId().equals(employee.getId())
                                &&
                                hw.getDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == currentWeek)
                        .mapToDouble(HoursWorked::getHours)
                        .sum();

                double updatedPendingHours = totalWorkedInSameWeek - hoursWorked.getHours();
                hoursWorked.setHoraspendientes(updatedPendingHours);
            }
        }

        int end = Math.min(start + pageSize, filteredHoursWorked.size());
        return filteredHoursWorked.subList(start, end);
    }

    private void initializeView() {
        getCurrentPageLayout().add(createAddHoursWorked());
        setupFilters();
        setupListHoursWorkedGrid();
        getCurrentPageLayout().add(hoursWorkedGrid);
    }

    private void setupFilters() {
        final HorizontalLayout hl = new HorizontalLayout();
        hl.add(createEmployeeFilter());
        hl.add(createTeamFilter());

        getCurrentPageLayout().add(hl);
    }

    private void setupListHoursWorkedGrid() {
        hoursWorkedGrid.addColumn(hw -> hw.getDate() != null ? hw.getDate().toString() : "")
                .setHeader("Fecha")
                .setSortable(true);
        hoursWorkedGrid.addColumn(HoursWorked::getWeekNumber)
                .setHeader("Semana")
                .setSortable(true);
        hoursWorkedGrid.addColumn(hw -> hw.getEmployee().getFirstName() + " " + hw.getEmployee().getLastName())
                .setHeader("Empleado");
        hoursWorkedGrid.addColumn(hw -> hw.getEmployee().getTeam() != null ? hw.getEmployee().getTeam()
                        .getName() : "Sin asignar")
                .setHeader("Equipo");
        hoursWorkedGrid.addColumn(hw -> {
            String actividad = hw.getActividad() != null ? hw.getActividad() : "Sin Actividad";
            String tareaEspecifica = hw.getTareaEspecifica() != null ? hw.getTareaEspecifica() : "";
            return !tareaEspecifica.isEmpty() ? tareaEspecifica : actividad;
        }).setHeader("Actividad");
        hoursWorkedGrid.addColumn(hw -> {
            if (hw.getTareaEspecifica() != null && !hw.getTareaEspecifica().isEmpty()) {
                return calcularHorasPorTareaEspecifica(hw);
            } else {
                return calcularHorasPorActividadGeneral(hw);
            }
        }).setHeader("Total Horas").setSortable(true);

        hoursWorkedGrid.addColumn(hw -> hw.getHoraspendientes() - calcularTotal(hw)).setHeader("Horas Pendientes")
                .setSortable(true);
        hoursWorkedGrid.addComponentColumn((ValueProvider<HoursWorked, Component>) hoursWorked -> {
            final MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
            final MenuItem viewItem = MenuBarUtils.createIconItem(menuBar, VaadinIcon.EYE, "Ver");
            viewItem.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
                navigateToHoursWorkedView(hoursWorked.getEmployee().getId());
            });
            return menuBar;
        });

        hoursWorkedGrid.setPaginationBarMode(PagingGrid.PaginationBarMode.BOTTOM);
        hoursWorkedGrid.setPageSize(PAGE_SIZE);
    }

    private double calcularHorasPorTareaEspecifica(final HoursWorked hoursWorked) {
        List<HoursWorked> tareas = hoursWorkedService.findListHoursWorkedEmployee(
                hoursWorked.getEmployee().getId(), hoursWorked.getWeekNumber());
        return tareas.stream()
                .filter(hw -> Objects.equals(hw.getTareaEspecifica(), hoursWorked.getTareaEspecifica()))
                .mapToDouble(HoursWorked::getHours)
                .sum();
    }

    private double calcularHorasPorActividadGeneral(final HoursWorked hoursWorked) {
        List<HoursWorked> actividades = hoursWorkedService.findListHoursWorkedEmployee(
                hoursWorked.getEmployee().getId(), hoursWorked.getWeekNumber());
        return actividades.stream()
                .filter(hw -> Objects.equals(hw.getActividad(), hoursWorked.getActividad())
                        && (hw.getTareaEspecifica() == null || hw.getTareaEspecifica().isEmpty()))
                .mapToDouble(HoursWorked::getHours)
                .sum();
    }

    private void navigateToHoursWorkedView(final UUID idEmployee) {
        getUI().ifPresent(ui -> ui.navigate("hours-worked-list/" + idEmployee.toString()));
    }

    private double calcularTotal(final HoursWorked hoursWorked) {
        List<HoursWorked> listHoursworkedemploye = hoursWorkedService.findListHoursWorkedEmployee(
                hoursWorked.getEmployee().getId(), hoursWorked.getWeekNumber());
        return calculateTotalUtilized(listHoursworkedemploye);
    }

    private double calculateTotalUtilized(final List<HoursWorked> employeeRequests) {
        return employeeRequests.stream()
                .filter(Objects::nonNull)
                .mapToDouble(HoursWorked::getHours)
                .sum();
    }

    private Button createButton(final String label, final Runnable onClickAction) {
        final Button button = new Button(label);
        button.addClickListener(event -> onClickAction.run());

        return button;
    }

    private Button createAddHoursWorked() {
        return createButton("Agregar Actividad", this::navigateToHours);
    }

    private void navigateToHours() {
        getUI().ifPresent(ui -> ui.navigate(HoursWorkedView.class, "new"));
    }

    private ComboBox<Employee> createEmployeeFilter() {
        employeeFilter = new ComboBox<>("Empleado");
        final List<Employee> employees = new ArrayList<>(employeeService.findAllEmployees());
        employees.addFirst(createAllEmployeesOption());
        employeeFilter.setItems(employees);
        employeeFilter.setItemLabelGenerator(this::getEmployeeFullName);
        employeeFilter.setValue(employees.getFirst());
        employeeFilter.addValueChangeListener(event ->
                refreshGridListHoursWorked(
                        event.getValue(),
                        teamFilter.getValue()
                )
        );

        return employeeFilter;
    }

    private String getEmployeeFullName(final Employee employee) {
        return "TODOS".equals(employee.getFirstName())
                ? "TODOS" : employee.getFirstName() + " " + employee.getLastName();
    }

    private ComboBox<Team> createTeamFilter() {
        teamFilter = new ComboBox<>("Equipo");
        List<Team> teams = new ArrayList<>(teamService.findAllTeams());
        teams.addFirst(createAllTeamsOption());
        teamFilter.setItems(teams);
        teamFilter.setItemLabelGenerator(this::getTeamLabel);
        teamFilter.setValue(teams.getFirst());
        teamFilter.addValueChangeListener(event ->
                refreshGridListHoursWorked(
                        employeeFilter.getValue(),
                        event.getValue()
                )
        );
        return teamFilter;
    }

    private String getTeamLabel(final Team team) {
        return team != null && !"TODOS".equals(team.getName()) ? team.getName() : "TODOS";
    }

    private Employee createAllEmployeesOption() {
        Employee allEmployeesOption = new Employee();
        allEmployeesOption.setFirstName("TODOS");
        return allEmployeesOption;
    }

    private Team createAllTeamsOption() {
        Team allTeamsOption = new Team();
        allTeamsOption.setName("TODOS");
        return allTeamsOption;
    }
}
