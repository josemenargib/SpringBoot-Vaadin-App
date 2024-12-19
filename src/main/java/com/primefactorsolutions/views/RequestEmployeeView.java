package com.primefactorsolutions.views;

import com.primefactorsolutions.model.*;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.service.TimeOffRequestService;
import com.primefactorsolutions.service.VacationService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
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
@PermitAll
@Scope("prototype")
@PageTitle("Employee Request")
@Route(value = "/requests", layout = MainLayout.class)
public class RequestEmployeeView extends BaseView implements HasUrlParameter<String> {

    private final TimeOffRequestService requestService;
    private final EmployeeService employeeService;
    private final VacationService vacationService;
    private final PagingGrid<TimeOffRequest> requestGrid = new PagingGrid<>(TimeOffRequest.class);
    private List<TimeOffRequest> requests = Collections.emptyList();
    private ComboBox<TimeOffRequestType> categoryFilter;
    private ComboBox<TimeOffRequestStatus> stateFilter;
    private UUID employeeId;

    public RequestEmployeeView(final TimeOffRequestService requestService,
                               final EmployeeService employeeService,
                               final VacationService vacationService) {
        this.requestService = requestService;
        this.employeeService = employeeService;
        this.vacationService = vacationService;
    }

    private void initializeView() {
        requestService.updateRequestStatuses();
        setupFilters();
        setupGrid();
        getCurrentPageLayout().add(requestGrid, new H3("Balance"), createSummaryLayout());
        refreshRequestGrid(null, null);
    }

    private void setupFilters() {
        categoryFilter = createCategoryFilter();
        stateFilter = createStateFilter();
        HorizontalLayout hl = new HorizontalLayout(categoryFilter, stateFilter);
        getCurrentPageLayout().add(hl);
    }

    private ComboBox<TimeOffRequestType> createCategoryFilter() {
        categoryFilter = new ComboBox<>("Categoría");
        categoryFilter.setItems(TimeOffRequestType.values());
        categoryFilter.setValue(TimeOffRequestType.values()[0]);
        categoryFilter.addValueChangeListener(event -> refreshRequestGrid(event.getValue(), stateFilter.getValue()));
        return categoryFilter;
    }

    private ComboBox<TimeOffRequestStatus> createStateFilter() {
        stateFilter = new ComboBox<>("Estado de la solicitud");
        stateFilter.setItems(TimeOffRequestStatus.values());
        stateFilter.setValue(TimeOffRequestStatus.values()[0]);
        stateFilter.addValueChangeListener(event -> refreshRequestGrid(categoryFilter.getValue(), event.getValue()));
        return stateFilter;
    }

    private void setupGrid() {
        requestGrid.setColumns(
                "category",
                "state",
                "startDate",
                "endDate",
                "daysToBeTake");

        requestGrid.getColumnByKey("category").setHeader("Categoría");
        requestGrid.getColumnByKey("state").setHeader("Estado");
        requestGrid.getColumnByKey("startDate").setHeader("Fecha de Inicio");
        requestGrid.getColumnByKey("endDate").setHeader("Fecha de Fin");
        requestGrid.getColumnByKey("daysToBeTake").setHeader("Días a Tomar");
        requestGrid.addComponentColumn((ValueProvider<TimeOffRequest, Component>) timeOffRequest -> {
            final MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
            final MenuItem view = createIconItem(menuBar, VaadinIcon.EYE, "View");
            view.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent ->
                    navigateToViewRequest(timeOffRequest));
            final MenuItem edit = createIconItem(menuBar, VaadinIcon.PENCIL, "Edit");
            edit.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent ->
                    navigateToEditRequest(timeOffRequest));
            return menuBar;
        });

        requestGrid.setPaginationBarMode(PagingGrid.PaginationBarMode.BOTTOM);
        requestGrid.setPageSize(PAGE_SIZE);
    }

    private Set<TimeOffRequestType> getStandardExclusions() {
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

    private Set<TimeOffRequestType> getMaleSpecificExclusions() {
        return Set.of(
                TimeOffRequestType.DIA_DE_LA_MUJER_INTERNACIONAL,
                TimeOffRequestType.DIA_DE_LA_MUJER_NACIONAL
        );
    }

    private VerticalLayout createSummaryLayout() {
        Employee employee = employeeService.getEmployee(employeeId);
        boolean isMale = employee.getGender() == Employee.Gender.MALE;
        int currentYear = LocalDate.now().getYear();

        List<Vacation> vacations = vacationService.findVacations();

        double healthLicence = 2;
        List<TimeOffRequest> healthRequests = requestService
                .findByEmployeeAndCategory(employeeId, TimeOffRequestType.PERMISOS_DE_SALUD);
        if (healthRequests != null && !healthRequests.isEmpty()) {
            healthLicence = healthRequests.getLast().getDaysBalance();
        }

        double totalFixedAndMovableHolidays = calculateHolidayDays(vacations);
        double totalPersonalDays = calculatePersonalDays(vacations, isMale);
        List<Double> vacationDays = calculateVacationDays(employee);

        double utilizedVacationCurrentDays = vacationDays.get(1);
        List<TimeOffRequest> vacationCurrentRequests = requestService
                .findByEmployeeAndCategory(employeeId, TimeOffRequestType.VACACION_GESTION_ACTUAL);
        if (vacationCurrentRequests != null && !vacationCurrentRequests.isEmpty()) {
            utilizedVacationCurrentDays = vacationCurrentRequests.getLast().getDaysBalance();
        }
        double totalVacationCurrentDays = vacationDays.get(1) - (vacationDays.get(1) - utilizedVacationCurrentDays);

        double utilizedVacationPreviousDays = vacationDays.get(0);
        List<TimeOffRequest> vacationPreviousRequests = requestService
                .findByEmployeeAndCategory(employeeId, TimeOffRequestType.VACACION_GESTION_ANTERIOR);
        if (vacationPreviousRequests != null && !vacationPreviousRequests.isEmpty()) {
            utilizedVacationPreviousDays = vacationPreviousRequests.getLast().getDaysBalance();
        }
        double totalVacationPreviousDays = vacationDays.getFirst()
                - (vacationDays.getFirst() - utilizedVacationPreviousDays);

        double utilizedFixedAndMovableHolidays = calculateHolidayUtilizedDays(currentYear);
        double utilizedPersonalDays = calculatePersonalDaysUtilized(isMale, currentYear);

        double remainingHolidayDays = totalFixedAndMovableHolidays - utilizedFixedAndMovableHolidays;
        double remainingPersonalDays = (totalPersonalDays - utilizedPersonalDays) + healthLicence;
        double remainingVacationDays = totalVacationCurrentDays + totalVacationPreviousDays;

        double totalAvailableDays = remainingHolidayDays + remainingPersonalDays + remainingVacationDays;

        return new VerticalLayout(
                new Span("Total feriados fijos y movibles: " + remainingHolidayDays),
                new Span("Total días libres personales: " + remainingPersonalDays),
                new Span("Total vacaciones pendientes de uso: " + remainingVacationDays),
                new Span("TOTAL GENERAL DE DÍAS DISPONIBLES: " + totalAvailableDays)
        );
    }

    private double calculateHolidayDays(final List<Vacation> vacations) {
        return vacations.stream()
                .filter(req -> req.getType() != Vacation.Type.OTHER)
                .mapToDouble(Vacation::getDuration)
                .sum();
    }

    private double calculatePersonalDays(final List<Vacation> vacations, final boolean isMale) {
        return vacations.stream()
                .filter(req -> req.getType() == Vacation.Type.OTHER)
                .filter(req -> !getStandardExclusions().contains(req.getCategory()))
                .filter(req -> !(isMale && getMaleSpecificExclusions().contains(req.getCategory())))
                .filter(req -> !req.getCategory().name().startsWith("VACACION"))
                .filter(req -> req.getCategory() != TimeOffRequestType.PERMISOS_DE_SALUD)
                .mapToDouble(Vacation::getDuration)
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

    private double calculateHolidayUtilizedDays(final int year) {
        return requests.stream()
                .filter(this::verificationIsHoliday)
                .filter(req -> req.getState() == TimeOffRequestStatus.TOMADO)
                .filter(req -> getStartDateYear(req) == year)
                .mapToDouble(TimeOffRequest::getDaysToBeTake)
                .sum();
    }

    private double calculatePersonalDaysUtilized(final boolean isMale, final int year) {
        return requests.stream()
                .filter(req -> !verificationIsHoliday(req))
                .filter(req -> req.getState() == TimeOffRequestStatus.TOMADO)
                .filter(req -> !getStandardExclusions().contains(req.getCategory()))
                .filter(req -> !(isMale && getMaleSpecificExclusions().contains(req.getCategory())))
                .filter(req -> !req.getCategory().name().startsWith("VACACION"))
                .filter(req -> req.getCategory() != TimeOffRequestType.PERMISOS_DE_SALUD)
                .filter(req -> getStartDateYear(req) == year)
                .mapToDouble(TimeOffRequest::getDaysToBeTake)
                .sum();
    }

    private int getStartDateYear(final TimeOffRequest request) {
        if (request.getStartDate() != null) {
            return request.getStartDate().getYear();
        }
        return 0;
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

    private Boolean verificationIsHoliday(final TimeOffRequest request) {
        Vacation vacation = vacationService.findVacationByCategory(request.getCategory());
        return vacation.getType() != Vacation.Type.OTHER;
    }

    private void navigateToRequestsListView() {
        getUI().ifPresent(ui -> ui.navigate(RequestsListView.class));
    }

    private void navigateToEditRequest(final TimeOffRequest request) {
        navigateToRequestView(request, "edit");
    }

    private void navigateToViewRequest(final TimeOffRequest request) {
        navigateToRequestView(request, "view");
    }

    private void navigateToRequestView(final TimeOffRequest request, final String action) {
        getUI().ifPresent(ui -> ui.navigate(RequestView.class, request.getId().toString() + "/" + action));
    }

    private void refreshRequestGrid(final TimeOffRequestType category, final TimeOffRequestStatus state) {
        requestGrid.setPagingDataProvider((page, pageSize) -> {
            int start = (int) (page * requestGrid.getPageSize());
            return fetchFilteredTimeOffRequests(start, pageSize, category, state);
        });
        requestGrid.getDataProvider().refreshAll();
    }

    private List<TimeOffRequest> fetchFilteredTimeOffRequests(final int start,
                                                              final int pageSize,
                                                              final TimeOffRequestType category,
                                                              final TimeOffRequestStatus state) {

        requests = requestService.findRequestsByEmployeeId(employeeId);
        generateRequests();
        if (category != null && !"TODOS".equals(category.name())) {
            requests = requests.stream()
                    .filter(req -> req.getCategory().equals(category))
                    .collect(Collectors.toList());
        }
        if (state != null && !"TODOS".equals(state.name())) {
            requests = requests.stream()
                    .filter(req -> req.getState().equals(state))
                    .collect(Collectors.toList());
        }
        int end = Math.min(start + pageSize, requests.size());
        return requests.subList(start, end);
    }

    public void generateRequests() {
        boolean isMale = isEmployeeMale();

        for (TimeOffRequestType type : TimeOffRequestType.values()) {
            if (shouldIncludeRequest(type) && isValidRequestType(type, isMale)) {
                TimeOffRequest request = createRequest(type);
                if (isVacationExpired(request)) {
                    request.setState(TimeOffRequestStatus.VENCIDO);
                } else {
                    request.setState(TimeOffRequestStatus.PENDIENTE);
                }
                requests.add(request);
            }
        }
    }

    private boolean isEmployeeMale() {
        return employeeService.getEmployee(employeeId).getGender() == Employee.Gender.MALE;
    }

    private boolean isValidRequestType(final TimeOffRequestType type, final boolean isMale) {
        return !getStandardExclusions().contains(type)
                && !(isMale && getMaleSpecificExclusions().contains(type))
                && type != TimeOffRequestType.TODOS;
    }

    private TimeOffRequest createRequest(final TimeOffRequestType type) {
        TimeOffRequest request = new TimeOffRequest();
        request.setCategory(type);
        return request;
    }

    private boolean isVacationExpired(final TimeOffRequest request) {
        Vacation vacation = vacationService.findVacationByCategory(request.getCategory());

        if (vacation != null && vacation.getMonthOfYear() != null && vacation.getDayOfMonth() != null) {
            int vacationMonth = vacation.getMonthOfYear();
            int vacationDay = vacation.getDayOfMonth();
            int currentMonth = LocalDate.now().getMonthValue();
            int currentDay = LocalDate.now().getDayOfMonth();

            return vacationMonth < currentMonth || (vacationMonth == currentMonth && vacationDay < currentDay);
        }

        return false;
    }

    private boolean shouldIncludeRequest(final TimeOffRequestType type) {
        List<TimeOffRequest> existingRequest = requestService.findByEmployeeAndCategory(employeeId, type);
        return existingRequest.isEmpty();
    }

    @Override
    public void setParameter(final BeforeEvent event, final String parameter) {
        employeeId = UUID.fromString(parameter);
        Employee employee = employeeService.getEmployee(employeeId);
        requests = requestService.findRequestsByEmployeeId(employeeId);
        setViewTitle(employee.getFirstName() + " " + employee.getLastName(), employee.getTeam().getName());
        requestGrid.setItems(requests);
        initializeView();
    }

    private void setViewTitle(final String employeeName, final String employeeTeam) {
        getCurrentPageLayout().addComponentAsFirst(new H3(String.format("%s (%s)", employeeName, employeeTeam)));
    }
}
