package com.primefactorsolutions.views;

import com.primefactorsolutions.model.*;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.service.TeamService;
import com.primefactorsolutions.service.TimeOffRequestService;
import com.primefactorsolutions.views.util.MenuBarUtils;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.primefactorsolutions.views.Constants.PAGE_SIZE;

@SpringComponent
@Scope("prototype")
@PageTitle("Pending Requests")
@Route(value = "/pending-requests", layout = MainLayout.class)
@PermitAll
public class PendingRequestsListView extends BaseView {

    private final TimeOffRequestService requestService;
    private final EmployeeService employeeService;
    private final TeamService teamService;
    private final PagingGrid<TimeOffRequest> pendingRequestsGrid = new PagingGrid<>();

    private ComboBox<Employee> employeeFilter;
    private ComboBox<Team> teamFilter;
    private ComboBox<TimeOffRequestType> categoryFilter;

    public PendingRequestsListView(final TimeOffRequestService requestService,
                                   final EmployeeService employeeService,
                                   final TeamService teamService) {
        this.requestService = requestService;
        this.employeeService = employeeService;
        this.teamService = teamService;
        initializeView();
        refreshGeneralPendingRequestsGrid(null, null, null);
    }

    private void initializeView() {
        setupFilters();
        setupPendingRequestsGrid();
    }

    private void setupFilters() {
        final HorizontalLayout hl = new HorizontalLayout();
        hl.add(createEmployeeFilter());
        hl.add(createTeamFilter());
        hl.add(createCategoryFilter());

        getCurrentPageLayout().add(hl);
    }

    private void setupPendingRequestsGrid() {
        pendingRequestsGrid.addColumn(this::getEmployeeFullName).setHeader("Empleado");
        pendingRequestsGrid.addColumn(this::getTeamName).setHeader("Equipo");
        pendingRequestsGrid.addColumn(this::getCategory).setHeader("Categoría");
        pendingRequestsGrid.addComponentColumn((ValueProvider<TimeOffRequest, Component>) timeOffRequest -> {
            final MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
            final MenuItem approveItem = MenuBarUtils.createIconItem(menuBar, VaadinIcon.CHECK, "Aprobar");
            approveItem.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
                actionForRequest(timeOffRequest.getId(), TimeOffRequestStatus.APROBADO);
            });
            final MenuItem rejectItem = MenuBarUtils.createIconItem(menuBar, VaadinIcon.BAN, "Rechazar");
            rejectItem.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
                actionForRequest(timeOffRequest.getId(), TimeOffRequestStatus.RECHAZADO);
            });
            return menuBar;
        });

        pendingRequestsGrid.setPaginationBarMode(PagingGrid.PaginationBarMode.BOTTOM);
        pendingRequestsGrid.setPageSize(PAGE_SIZE);

        getCurrentPageLayout().add(pendingRequestsGrid);
    }

    private void actionForRequest(final UUID selectedRequestId, final TimeOffRequestStatus status) {
        TimeOffRequest request = requestService.findTimeOffRequest(selectedRequestId);
        request.setState(status);
        requestService.saveTimeOffRequest(request);
        refreshGeneralPendingRequestsGrid(null, null, null);
    }

    private void refreshGeneralPendingRequestsGrid(final Employee employee,
                                           final Team team,
                                           final TimeOffRequestType category) {
        pendingRequestsGrid.setPagingDataProvider((page, pageSize) -> {
            int start = (int) (page * pendingRequestsGrid.getPageSize());
            return fetchFilteredPendingRequests(start, pageSize, employee, team, category);
        });
        pendingRequestsGrid.getDataProvider().refreshAll();
    }

    private List<TimeOffRequest> fetchFilteredPendingRequests(final int start,
                                                  final int pageSize,
                                                  final Employee employee,
                                                  final Team team,
                                                  final TimeOffRequestType category) {
        List<TimeOffRequest> filteredPendingRequests
                = requestService.findRequestsByState(TimeOffRequestStatus.SOLICITADO);

        if (employee != null && !"TODOS".equals(employee.getFirstName())) {
            filteredPendingRequests = filteredPendingRequests.stream()
                    .filter(emp -> emp.getEmployee().getId().equals(employee.getId()))
                    .collect(Collectors.toList());
        }

        if (team != null && !"TODOS".equals(team.getName())) {
            filteredPendingRequests = filteredPendingRequests.stream()
                    .filter(emp -> emp.getEmployee().getTeam() != null
                            && emp.getEmployee().getTeam().getId().equals(team.getId()))
                    .collect(Collectors.toList());
        }

        if (category != null && category != TimeOffRequestType.TODOS) {
            filteredPendingRequests = filteredPendingRequests.stream()
                    .filter(emp -> emp.getCategory().equals(category))
                    .collect(Collectors.toList());
        }

        int end = Math.min(start + pageSize, filteredPendingRequests.size());
        return filteredPendingRequests.subList(start, end);
    }

    private String getEmployeeFullName(final TimeOffRequest request) {
        Employee employee = request.getEmployee();
        return getEmployeeFullNameLabel(employee);
    }

    private String getEmployeeFullNameLabel(final Employee employee) {
        return "TODOS".equals(employee.getFirstName())
                ? "TODOS" : employee.getFirstName() + " " + employee.getLastName();
    }

    private String getTeamName(final TimeOffRequest request) {
        Team team = request.getEmployee().getTeam();
        return team != null ? team.getName() : "Sin asignar";
    }

    private String getTeamLabel(final Team team) {
        return "TODOS".equals(team.getName()) ? "TODOS" : team.getName();
    }

    private String getCategory(final TimeOffRequest request) {
        return String.valueOf(request.getCategory());
    }

    private ComboBox<Employee> createEmployeeFilter() {
        employeeFilter = new ComboBox<>("Empleado");
        List<Employee> employees = new ArrayList<>(employeeService.findAllEmployees());
        employees.addFirst(createAllEmployeesOption());
        employeeFilter.setItems(employees);
        employeeFilter.setItemLabelGenerator(this::getEmployeeFullNameLabel);
        employeeFilter.setValue(employees.getFirst());
        employeeFilter.addValueChangeListener(event ->
                refreshGeneralPendingRequestsGrid(
                        event.getValue(),
                        teamFilter.getValue(),
                        categoryFilter.getValue()
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
                refreshGeneralPendingRequestsGrid(
                        employeeFilter.getValue(),
                        event.getValue(),
                        categoryFilter.getValue()
                )
        );
        return teamFilter;
    }

    private ComboBox<TimeOffRequestType> createCategoryFilter() {
        categoryFilter = new ComboBox<>("Categoría");
        categoryFilter.setItems(TimeOffRequestType.values());
        categoryFilter.setValue(TimeOffRequestType.values()[0]);
        categoryFilter.addValueChangeListener(event ->
                refreshGeneralPendingRequestsGrid(
                        employeeFilter.getValue(),
                        teamFilter.getValue(),
                        event.getValue()
                )
        );
        return categoryFilter;
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