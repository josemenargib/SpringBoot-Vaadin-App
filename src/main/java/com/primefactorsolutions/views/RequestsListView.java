package com.primefactorsolutions.views;

import com.primefactorsolutions.model.*;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.service.TeamService;
import com.primefactorsolutions.service.TimeOffRequestService;
import com.primefactorsolutions.service.VacationService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.components.grid.PagingGrid;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static com.primefactorsolutions.views.Constants.PAGE_SIZE;
import static com.primefactorsolutions.views.util.MenuBarUtils.createIconItem;

@SpringComponent
@Scope("prototype")
@PageTitle("Requests")
@Route(value = "/requests", layout = MainLayout.class)
@PermitAll
public class RequestsListView extends BaseView {

    private final TimeOffRequestService requestService;
    private final EmployeeService employeeService;
    private final TeamService teamService;
    private final VacationService vacationService;
    private final PagingGrid<Employee> requestGrid = new PagingGrid<>();

    private ComboBox<Employee> employeeFilter;
    private ComboBox<Team> teamFilter;
    private ComboBox<Status> stateFilter;

    public RequestsListView(final TimeOffRequestService requestService,
                            final EmployeeService employeeService,
                            final TeamService teamService,
                            final VacationService vacationService) {
        this.requestService = requestService;
        this.employeeService = employeeService;
        this.teamService = teamService;
        this.vacationService = vacationService;
        initializeView();
        refreshGeneralRequestGrid(null, null, null);
    }

    private void initializeView() {
        requestService.updateRequestStatuses();
        setupFilters();
        setupRequestGrid();
        getCurrentPageLayout().add(requestGrid);
    }

    private void setupFilters() {
        final HorizontalLayout hl = new HorizontalLayout();
        hl.add(createEmployeeFilter());
        hl.add(createTeamFilter());
        hl.add(createStateFilter());

        getCurrentPageLayout().add(hl);
    }

    private void setupRequestGrid() {
        requestGrid.addColumn(this::getEmployeeFullName).setHeader("Empleado");
        requestGrid.addColumn(this::getTeamName).setHeader("Equipo");
        requestGrid.addColumn(this::getEmployeeStatus).setHeader("Estado del empleado");
        requestGrid.addColumn(this::getGeneralTotal).setHeader("Total general");
        requestGrid.addComponentColumn((ValueProvider<Employee, Component>) employee -> {
            final MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
            final MenuItem view = createIconItem(menuBar, VaadinIcon.EYE, "View");
            view.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent ->
                    navigateToTimeOffRequestView(employee.getId()));

            return menuBar;
        });

        requestGrid.setPaginationBarMode(PagingGrid.PaginationBarMode.BOTTOM);
        requestGrid.setPageSize(PAGE_SIZE);
    }

    private void refreshGeneralRequestGrid(final Employee employee,
                                           final Team team,
                                           final Status state) {
        requestGrid.setPagingDataProvider((page, pageSize) -> {
            int start = (int) (page * requestGrid.getPageSize());
            return fetchFilteredEmployees(start, pageSize, employee, team, state);
        });
        requestGrid.getDataProvider().refreshAll();
    }

    private List<Employee> fetchFilteredEmployees(final int start,
                                                  final int pageSize,
                                                  final Employee employee,
                                                  final Team team,
                                                  final Status state) {
        List<Employee> filteredEmployees = employeeService.findAllEmployees();

        if (employee != null && !"TODOS".equals(employee.getFirstName())) {
            filteredEmployees = filteredEmployees.stream()
                    .filter(emp -> emp.getId().equals(employee.getId()))
                    .collect(Collectors.toList());
        }

        if (team != null && !"TODOS".equals(team.getName())) {
            filteredEmployees = filteredEmployees.stream()
                    .filter(emp -> emp.getTeam() != null && emp.getTeam().getId().equals(team.getId()))
                    .collect(Collectors.toList());
        }

        if (state != null && state != Status.TODOS) {
            filteredEmployees = filteredEmployees.stream()
                    .filter(emp -> {
                        Optional<TimeOffRequest> request = requestService
                                .findByEmployeeAndState(emp.getId(), TimeOffRequestStatus.EN_USO);
                        return state == Status.EN_DESCANSO ? request.isPresent() : request.isEmpty();
                    })
                    .collect(Collectors.toList());
        }

        int end = Math.min(start + pageSize, filteredEmployees.size());
        return filteredEmployees.subList(start, end);
    }

    private String getEmployeeFullName(final Employee employee) {
        return "TODOS".equals(employee.getFirstName())
                ? "TODOS" : employee.getFirstName() + " " + employee.getLastName();
    }

    private String getTeamName(final Employee employee) {
        Team team = employee.getTeam();
        return team != null ? team.getName() : "Sin asignar";
    }

    private String getTeamLabel(final Team team) {
        return "TODOS".equals(team.getName()) ? "TODOS" : team.getName();
    }

    private String getEmployeeStatus(final Employee employee) {
        Optional<TimeOffRequest> activeRequest = requestService
                .findByEmployeeAndState(employee.getId(), TimeOffRequestStatus.EN_USO);
        return activeRequest.isPresent() ? "EN_DESCANSO" : "ACTIVO";
    }

    private String getGeneralTotal(final Employee employee) {
        List<TimeOffRequest> employeeRequests = requestService.findRequestsByEmployeeId(employee.getId());
        List<Vacation> vacations = vacationService.findVacations();

        List<Double> vacationDays = calculateVacationDays(employee);

        double utilizedVacationCurrentDays = vacationDays.get(1);
        List<TimeOffRequest> vacationCurrentRequests = requestService
                .findByEmployeeAndCategory(employee.getId(), TimeOffRequestType.VACACION_GESTION_ACTUAL);
        if (vacationCurrentRequests != null && !vacationCurrentRequests.isEmpty()) {
            utilizedVacationCurrentDays = vacationCurrentRequests.getLast().getDaysBalance();
        }
        double totalVacationCurrentDays = vacationDays.get(1) - (vacationDays.get(1) - utilizedVacationCurrentDays);

        double utilizedVacationPreviousDays = vacationDays.get(0);
        List<TimeOffRequest> vacationPreviousRequests = requestService
                .findByEmployeeAndCategory(employee.getId(), TimeOffRequestType.VACACION_GESTION_ANTERIOR);
        if (vacationPreviousRequests != null && !vacationPreviousRequests.isEmpty()) {
            utilizedVacationPreviousDays = vacationPreviousRequests.getLast().getDaysBalance();
        }
        double totalVacationPreviousDays = vacationDays.getFirst()
                - (vacationDays.getFirst() - utilizedVacationPreviousDays);


        double totalUtilized = calculateTotalUtilized(employeeRequests);
        double totalVacations = totalVacationCurrentDays + totalVacationPreviousDays;
        double totalAvailable = calculateTotalAvailable(vacations, employeeRequests, employee);

        double generalTotal = totalAvailable + totalVacations - totalUtilized;
        return String.valueOf(generalTotal);
    }

    private Set<TimeOffRequestType> getExcludedCategories() {
        return Set.of(
                TimeOffRequestType.MATERNIDAD,
                TimeOffRequestType.PATERNIDAD,
                TimeOffRequestType.MATRIMONIO,
                TimeOffRequestType.DUELO_1ER_GRADO,
                TimeOffRequestType.DUELO_2ER_GRADO,
                TimeOffRequestType.DIA_DEL_PADRE,
                TimeOffRequestType.DIA_DE_LA_MADRE
        );
    }

    private Set<TimeOffRequestType> getGenderSpecificExclusions() {
        return Set.of(
                TimeOffRequestType.DIA_DE_LA_MUJER_INTERNACIONAL,
                TimeOffRequestType.DIA_DE_LA_MUJER_NACIONAL
        );
    }

    private double calculateTotalUtilized(final List<TimeOffRequest> employeeRequests) {
        int currentYear = LocalDate.now().getYear();
        return employeeRequests.stream()
                .filter(Objects::nonNull)
                .filter(request -> request.getCategory() != TimeOffRequestType.PERMISOS_DE_SALUD)
                .filter(request -> request.getCategory() != TimeOffRequestType.VACACION_GESTION_ACTUAL)
                .filter(request -> request.getCategory() != TimeOffRequestType.VACACION_GESTION_ANTERIOR)
                .filter(request -> request.getStartDate() != null && (
                        request.getStartDate().getYear() == currentYear
                                || (request.getCategory().name().startsWith("VACACION")
                                && request.getStartDate().getYear() == currentYear - 1)
                ))
                .mapToDouble(request -> request.getDaysToBeTake() != null ? request.getDaysToBeTake() : 0.0)
                .sum();
    }

    private List<Double> calculateVacationDays(final Employee employee) {
        List<Double> vacationDays = new ArrayList<>();

        if (employee.getDateOfEntry() != null) {
            LocalDate entryDate = employee.getDateOfEntry();
            LocalDate today = LocalDate.now();

            boolean hasAnniversaryPassed = entryDate.getMonthValue() < today.getMonthValue()
                    || (entryDate.getMonthValue() == today.getMonthValue() && entryDate.getDayOfMonth()
                    <= today.getDayOfMonth());

            LocalDate previousVacationYearDate;
            LocalDate currentVacationYearDate;

            if (hasAnniversaryPassed) {
                previousVacationYearDate = LocalDate.of(
                        today.getYear() - 1,
                        entryDate.getMonth(),
                        entryDate.getDayOfMonth()
                );
                currentVacationYearDate = LocalDate.of(
                        today.getYear(),
                        entryDate.getMonth(),
                        entryDate.getDayOfMonth()
                );
            } else {
                previousVacationYearDate = LocalDate.of(
                        today.getYear() - 2,
                        entryDate.getMonth(),
                        entryDate.getDayOfMonth()
                );
                currentVacationYearDate = LocalDate.of(
                        today.getYear() - 1,
                        entryDate.getMonth(),
                        entryDate.getDayOfMonth()
                );
            }

            vacationDays.add(calculateVacationDaysSinceEntry(entryDate, previousVacationYearDate));
            vacationDays.add(calculateVacationDaysSinceEntry(entryDate, currentVacationYearDate));
        } else {
            vacationDays.add(0.0);
            vacationDays.add(0.0);
        }
        return vacationDays;
    }

    private double calculateTotalAvailable(final List<Vacation> vacations, final List<TimeOffRequest> employeeRequests,
                                           final Employee employee) {
        Set<TimeOffRequestType> excludedCategories = getExcludedCategories();
        Set<TimeOffRequestType> genderSpecificExclusions = getGenderSpecificExclusions();
        Set<TimeOffRequestType> employeeRequestCategories = employeeRequests.stream()
                .map(TimeOffRequest::getCategory)
                .collect(Collectors.toSet());

        double healthLicence = 2;
        List<TimeOffRequest> healthRequests = requestService
                .findByEmployeeAndCategory(employee.getId(), TimeOffRequestType.PERMISOS_DE_SALUD);
        if (healthRequests != null && !healthRequests.isEmpty()) {
            healthLicence = healthRequests.getLast().getDaysBalance();
        }

        double totalAvailable = vacations.stream()
                .filter(Objects::nonNull)
                .filter(vacation -> vacation.getCategory() != TimeOffRequestType.PERMISOS_DE_SALUD)
                .filter(vacation -> shouldIncludeVacation(
                        vacation,
                        excludedCategories,
                        genderSpecificExclusions,
                        employee, employeeRequestCategories
                ))
                .mapToDouble(vacation -> vacation.getDuration() != null ? vacation.getDuration() : 0.0)
                .sum();

        return totalAvailable + healthLicence;
    }

    private double calculateVacationDaysSinceEntry(final LocalDate dateOfEntry, final LocalDate date) {
        int yearsOfService = dateOfEntry != null ? Period.between(dateOfEntry, date).getYears() : 0;
        if (yearsOfService > 10) {
            return 30;
        }
        if (yearsOfService > 5) {
            return 20;
        }
        if (yearsOfService > 1) {
            return 15;
        }
        return 0;
    }

    private boolean shouldIncludeVacation(final Vacation vacation,
                                          final Set<TimeOffRequestType> excludedCategories,
                                          final Set<TimeOffRequestType> genderSpecificExclusions,
                                          final Employee employee,
                                          final Set<TimeOffRequestType> employeeRequestCategories) {
        if (excludedCategories.contains(vacation.getCategory())
                && !employeeRequestCategories.contains(vacation.getCategory())) {
            return false;
        }

        return isFemale(employee) || !genderSpecificExclusions.contains(vacation.getCategory());
    }

    private boolean isFemale(final Employee employee) {
        return employee.getGender() == Employee.Gender.FEMALE;
    }

    private ComboBox<Employee> createEmployeeFilter() {
        employeeFilter = new ComboBox<>("Empleado");
        List<Employee> employees = new ArrayList<>(employeeService.findAllEmployees());
        employees.addFirst(createAllEmployeesOption());
        employeeFilter.setItems(employees);
        employeeFilter.setItemLabelGenerator(this::getEmployeeFullName);
        employeeFilter.setValue(employees.getFirst());
        employeeFilter.addValueChangeListener(event ->
                refreshGeneralRequestGrid(
                        event.getValue(),
                        teamFilter.getValue(),
                        stateFilter.getValue()
                )
        );
        return employeeFilter;
    }

    private ComboBox<Team> createTeamFilter() {
        teamFilter = new ComboBox<>("Equipo");
        List<Team> teams = new ArrayList<>(teamService.findAllTeams());
        teams.addFirst(createAllTeamsOption());
        teamFilter.setItems(teams);
        teamFilter.setItemLabelGenerator(this::getTeamLabel);
        teamFilter.setValue(teams.getFirst());
        teamFilter.addValueChangeListener(event ->
                refreshGeneralRequestGrid(
                        employeeFilter.getValue(),
                        event.getValue(),
                        stateFilter.getValue()
                )
        );
        return teamFilter;
    }

    private ComboBox<Status> createStateFilter() {
        stateFilter = new ComboBox<>("Estado del empleado");
        stateFilter.setItems(Status.values());
        stateFilter.setValue(Status.values()[0]);
        stateFilter.addValueChangeListener(event ->
                refreshGeneralRequestGrid(
                        employeeFilter.getValue(),
                        teamFilter.getValue(),
                        event.getValue()
                )
        );
        return stateFilter;
    }

    private enum Status {
        TODOS,
        EN_DESCANSO,
        ACTIVO
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

    private void navigateToMainView() {
        getUI().ifPresent(ui -> ui.navigate(MainView.class));
    }

    private void navigateToTimeOffRequestView(final UUID idEmployee) {
        getUI().ifPresent(ui -> ui.navigate("requests/" + idEmployee.toString()));
    }
}