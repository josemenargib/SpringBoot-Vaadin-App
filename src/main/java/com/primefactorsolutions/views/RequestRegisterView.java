package com.primefactorsolutions.views;

import com.primefactorsolutions.model.*;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.service.TimeOffRequestService;
import com.primefactorsolutions.service.VacationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@SpringComponent
@PermitAll
@Scope("prototype")
@PageTitle("Request")
@Route(value = "/requests/new", layout = MainLayout.class)
public class RequestRegisterView extends VerticalLayout {

    private final ComboBox<Employee> employeeComboBox = new ComboBox<>("Empleado");
    private final ComboBox<TimeOffRequestType> categoryComboBox = new ComboBox<>("Categoría");
    private final NumberField availableDaysField = new NumberField("Días disponibles");
    private final DatePicker startDatePicker = new DatePicker("Fecha de inicio");
    private final DatePicker endDatePicker = new DatePicker("Fecha final");
    private final NumberField daysToBeTakenField = new NumberField("Días a tomar");
    private final NumberField balanceDaysField = new NumberField("Días de saldo");

    private final TimeOffRequestService requestService;
    private final EmployeeService employeeService;
    private final VacationService vacationService;

    private final Binder<TimeOffRequest> binder;
    private Vacation vacation;
    private Employee employee;
    private LocalDate endDate;

    private Button saveButton;
    private Button closeButton;

    public RequestRegisterView(final TimeOffRequestService requestService,
                               final EmployeeService employeeService,
                               final VacationService vacationService) {
        this.requestService = requestService;
        this.employeeService = employeeService;
        this.vacationService = vacationService;
        this.binder = new Binder<>(TimeOffRequest.class);
        initializeView();
    }

    private void initializeView() {
        requestService.updateRequestStatuses();
        configureFormFields();
        configureButtons();
        configureBinder();
        setupFormLayout();
        configureInitialFieldStates();
    }

    private void configureInitialFieldStates() {
        categoryComboBox.setEnabled(false);
        startDatePicker.setEnabled(false);
        endDatePicker.setEnabled(false);
        availableDaysField.setReadOnly(true);
        daysToBeTakenField.setReadOnly(true);
        balanceDaysField.setReadOnly(true);
    }

    private void configureFormFields() {
        employeeComboBox.setItems(employeeService.findAllEmployees());
        employeeComboBox.setItemLabelGenerator(emp -> emp.getFirstName() + " " + emp.getLastName());
        employeeComboBox.addValueChangeListener(event -> {
            employee = event.getValue();
            handleEmployeeSelection(event.getValue());
        });
        categoryComboBox.addValueChangeListener(event -> {
            onCategoryChange(event.getValue());
            handleCategorySelection(event.getValue());
        });
        startDatePicker.addValueChangeListener(event -> updateDatePickerMinValues());
        endDatePicker.addValueChangeListener(event -> calculateDays());
    }

    private void configureBinder() {
        binder.forField(employeeComboBox)
                .bind(TimeOffRequest::getEmployee, TimeOffRequest::setEmployee);
        binder.forField(categoryComboBox)
                .bind(TimeOffRequest::getCategory, TimeOffRequest::setCategory);
        binder.forField(availableDaysField)
                .bind(TimeOffRequest::getAvailableDays, TimeOffRequest::setAvailableDays);
        binder.forField(startDatePicker)
                .bind(TimeOffRequest::getStartDate, TimeOffRequest::setStartDate);
        binder.forField(endDatePicker)
                .bind(TimeOffRequest::getEndDate, TimeOffRequest::setEndDate);
        binder.forField(daysToBeTakenField)
                .bind(TimeOffRequest::getDaysToBeTake, TimeOffRequest::setDaysToBeTake);
        binder.forField(balanceDaysField)
                .bind(TimeOffRequest::getDaysBalance, TimeOffRequest::setDaysBalance);
        binder.setBean(new TimeOffRequest());
    }

    private void handleEmployeeSelection(final Employee selectedEmployee) {
        if (selectedEmployee != null) {
            categoryComboBox.clear();
            availableDaysField.clear();
            startDatePicker.clear();
            endDatePicker.clear();
            daysToBeTakenField.clear();
            balanceDaysField.clear();
            categoryComboBox.setEnabled(true);
            startDatePicker.setEnabled(false);
            endDatePicker.setEnabled(false);
            filterCategories(selectedEmployee);
        }
    }

    private void filterCategories(final Employee employee) {
        categoryComboBox.clear();
        List<TimeOffRequest> employeeRequests = requestService.findRequestsByEmployeeId(employee.getId());
        List<TimeOffRequestType> allCategories = Arrays.asList(TimeOffRequestType.values());
        List<TimeOffRequestType> availableCategories = allCategories.stream()
                .filter(category -> isCategoryAvailable(employeeRequests, category))
                .filter(category -> isCategoryAllowedByGender(category, employee.getGender()))
                .filter(category -> category != TimeOffRequestType.TODOS)
                .filter(category -> shouldIncludeVacationGestionActual(employeeRequests, category))
                .filter(category -> shouldIncludeVacationGestionAnterior(employeeRequests, category))
                .toList();

        categoryComboBox.setItems(availableCategories);
    }


    private boolean shouldIncludeVacationGestionActual(final List<TimeOffRequest> employeeRequests,
                                                       final TimeOffRequestType category) {
        if (category != TimeOffRequestType.VACACION_GESTION_ACTUAL) {
            return true;
        }
        return employeeRequests.stream()
                .anyMatch(request -> request.getCategory() == TimeOffRequestType.VACACION_GESTION_ANTERIOR
                        && request.getDaysBalance() == 0
                        && request.getState() == TimeOffRequestStatus.TOMADO);
    }

    private boolean shouldIncludeVacationGestionAnterior(final List<TimeOffRequest> employeeRequests,
                                                         final TimeOffRequestType category) {
        if (category != TimeOffRequestType.VACACION_GESTION_ANTERIOR) {
            return true;
        }
        return employeeRequests.stream()
                .noneMatch(request -> request.getCategory() == TimeOffRequestType.VACACION_GESTION_ANTERIOR
                        && request.getDaysBalance() == 0);
    }

    private void onCategoryChange(final TimeOffRequestType selectedCategory) {
        if (selectedCategory == TimeOffRequestType.VACACION_GESTION_ACTUAL
            || selectedCategory == TimeOffRequestType.VACACION_GESTION_ANTERIOR) {
            startDatePicker.setEnabled(true);
            endDatePicker.setEnabled(true);
        } else {
            startDatePicker.setEnabled(true);
            endDatePicker.setEnabled(false);
        }
    }

    private boolean isCategoryAvailable(final List<TimeOffRequest> employeeRequests,
                                        final TimeOffRequestType category) {
        List<TimeOffRequest> requestsByCategory = employeeRequests.stream()
                .filter(request -> request.getCategory() == category)
                .toList();

        if (requestsByCategory.isEmpty()) {
            return true;
        }

        TimeOffRequest latestRequest = requestsByCategory.stream()
                .max(Comparator.comparing(TimeOffRequest::getStartDate))
                .orElse(null);

        boolean isSpecialCategory = category == TimeOffRequestType.PERMISOS_DE_SALUD
                || category == TimeOffRequestType.VACACION_GESTION_ACTUAL
                || category == TimeOffRequestType.VACACION_GESTION_ANTERIOR;

        if (isSpecialCategory) {
            return (latestRequest.getState() == TimeOffRequestStatus.TOMADO
                    && latestRequest.getDaysBalance() > 0)
                    || latestRequest.getState() == TimeOffRequestStatus.RECHAZADO
                    || (latestRequest.getState() == TimeOffRequestStatus.TOMADO
                        && latestRequest.getDaysBalance() == 0
                        && latestRequest.getExpiration().isBefore(LocalDate.now()));
        } else {
            return (latestRequest.getState() == TimeOffRequestStatus.TOMADO
                    && latestRequest.getExpiration().isBefore(LocalDate.now()))
                    || latestRequest.getState() == TimeOffRequestStatus.RECHAZADO;
        }
    }

    private boolean isCategoryAllowedByGender(final TimeOffRequestType category, final Employee.Gender gender) {
        if (gender == Employee.Gender.MALE) {
            return category != TimeOffRequestType.MATERNIDAD
                    && category != TimeOffRequestType.DIA_DE_LA_MADRE
                    && category != TimeOffRequestType.DIA_DE_LA_MUJER_INTERNACIONAL
                    && category != TimeOffRequestType.DIA_DE_LA_MUJER_NACIONAL;
        } else {
            return category != TimeOffRequestType.DIA_DEL_PADRE
                    && category != TimeOffRequestType.PATERNIDAD;
        }
    }

    private void handleCategorySelection(final TimeOffRequestType selectedCategory) {
        if (selectedCategory != null) {
            updateAvailableDays(selectedCategory);
        }
    }

    private void updateAvailableDays(final TimeOffRequestType selectedCategory) {
        vacation = vacationService.findVacationByCategory(selectedCategory);
        UUID employeeId = employeeComboBox.getValue().getId();
        List<TimeOffRequest> requests = requestService.findByEmployeeAndCategory(employeeId, selectedCategory);

        if (vacation != null) {
            TimeOffRequest requestWithBalance = requests.stream()
                    .filter(request -> request.getDaysBalance() > 0
                            && request.getState() != TimeOffRequestStatus.VENCIDO
                            && request.getState() != TimeOffRequestStatus.RECHAZADO)
                    .max(Comparator.comparing(TimeOffRequest::getStartDate))
                    .orElse(null);

            if (requestWithBalance != null) {
                if (requestWithBalance.getState() == TimeOffRequestStatus.TOMADO
                        && requestWithBalance.getDaysBalance() > 0) {
                    availableDaysField.setValue(requestWithBalance.getDaysBalance());
                } else {
                    availableDaysField.setValue(vacation.getDuration());
                }
            } else if (selectedCategory == TimeOffRequestType.VACACION_GESTION_ACTUAL
                    || selectedCategory == TimeOffRequestType.VACACION_GESTION_ANTERIOR) {
                LocalDate dateOfEntry = employeeComboBox.getValue().getDateOfEntry();
                LocalDate currentDate = LocalDate.now();
                long yearsOfService = ChronoUnit.YEARS.between(dateOfEntry, currentDate);

                if (selectedCategory == TimeOffRequestType.VACACION_GESTION_ANTERIOR) {
                    yearsOfService -= 1;
                }

                if (yearsOfService > 10) {
                    availableDaysField.setValue(30.0);
                } else if (yearsOfService > 5) {
                    availableDaysField.setValue(20.0);
                } else if (yearsOfService > 1) {
                    availableDaysField.setValue(15.0);
                } else {
                    availableDaysField.setValue(0.0);
                }
            } else {
                availableDaysField.setValue(vacation.getDuration());
            }
            setDatePickerLimits(vacation);
        }
    }

    private void setDatePickerLimits(final Vacation vacation) {
        LocalDate startDate;
        endDate = null;

        UUID employeeId = employee.getId();
        List<TimeOffRequest> previousRequests
                = requestService.findByEmployeeAndCategory(employeeId, vacation.getCategory());

        int startYear = calculateStartYear(previousRequests);

        startDate = determineStartDate(vacation, startYear);

        if (startDate.isBefore(LocalDate.now())) {
            startDate = determineStartDate(vacation, startYear + 1);
        }

        if (startDate != null) {
            if (vacation.getExpiration() != null) {
                endDate = startDate.plusDays(vacation.getExpiration().intValue() - 1);
            } else {
                endDate = LocalDate.of(startDate.getYear(), 12, 31);
            }
        } else {
            startDate = LocalDate.now();
        }

        setPickerValues(vacation, startDate);
        setPickerLimits(startDate, endDate);
    }

    private int calculateStartYear(final List<TimeOffRequest> previousRequests) {
        if (previousRequests.isEmpty()) {
            return LocalDate.now().getYear();
        }

        int lastRequestYear = previousRequests.stream()
                .max(Comparator.comparing(TimeOffRequest::getStartDate))
                .map(request -> request.getStartDate().getYear())
                .orElse(LocalDate.now().getYear());

        if (previousRequests.getLast().getState() != TimeOffRequestStatus.RECHAZADO) {
            lastRequestYear = lastRequestYear + 1;
        }

        int currentYear = LocalDate.now().getYear();
        return Math.max(lastRequestYear, currentYear);
    }

    private LocalDate determineStartDate(final Vacation vacation, final int startYear) {
        if (vacation.getCategory() == TimeOffRequestType.CUMPLEAÑOS && employee.getBirthday() != null) {
            return LocalDate.of(startYear, employee.getBirthday().getMonth(), employee.getBirthday().getDayOfMonth());
        }

        if (vacation.getMonthOfYear() != null && vacation.getDayOfMonth() != null) {
            return LocalDate.of(startYear, vacation.getMonthOfYear().intValue(), vacation.getDayOfMonth().intValue());
        }

        if (vacation.getCategory() == TimeOffRequestType.PERMISOS_DE_SALUD) {
            return LocalDate.now();
        }

        return LocalDate.now();
    }

    private void setPickerValues(final Vacation vacation, final LocalDate startDate) {
        startDatePicker.setValue(startDate);

        if ((vacation.getDuration() != null && vacation.getDuration() == 0.5)
                || vacation.getCategory() == TimeOffRequestType.PERMISOS_DE_SALUD
                || vacation.getCategory() == TimeOffRequestType.CUMPLEAÑOS) {

            endDatePicker.setValue(startDate);
        } else {
            int durationDays = (vacation.getDuration() != null ? vacation.getDuration().intValue() - 1 : 0);
            endDatePicker.setValue(startDate.plusDays(durationDays));
        }
    }

    private void setPickerLimits(final LocalDate startDate, final LocalDate endDate) {
        startDatePicker.setMin(startDate);
        startDatePicker.setMax(endDate);
        endDatePicker.setMin(startDate);
        endDatePicker.setMax(endDate);
    }

    private void updateDatePickerMinValues() {
        LocalDate startDate = startDatePicker.getValue();
        if (availableDaysField.getValue() != null) {
            if (availableDaysField.getValue() == 0.5) {
                endDatePicker.setValue(startDate.plusDays(0));
            } else {
                endDatePicker.setValue(startDate.plusDays(availableDaysField.getValue().intValue() - 1));
            }
            calculateDays();
        }
    }

    private void calculateDays() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        Double availableDays = availableDaysField.getValue();

        if (areDatesValid(startDate, endDate)) {
            double daysToBeTaken = calculateDaysBetween(startDate, endDate);
            setDaysToBeTakenField(daysToBeTaken);

            double balanceDays = calculateBalanceDays(availableDays, daysToBeTakenField.getValue());
            balanceDaysField.setValue(balanceDays);

            if (balanceDays < 0.0) {
                clearFields();
            }
        }
    }

    private boolean areDatesValid(final LocalDate startDate, final LocalDate endDate) {
        return startDate != null && endDate != null;
    }

    private double calculateDaysBetween(final LocalDate startDate, final LocalDate endDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    private void setDaysToBeTakenField(final double daysToBeTaken) {
        if (vacation.getCategory() == TimeOffRequestType.PERMISOS_DE_SALUD
                || vacation.getCategory() == TimeOffRequestType.CUMPLEAÑOS
                || vacation.getCategory() == TimeOffRequestType.DIA_DEL_PADRE
                || vacation.getCategory() == TimeOffRequestType.DIA_DE_LA_MADRE
                || vacation.getCategory() == TimeOffRequestType.DIA_DE_LA_MUJER_INTERNACIONAL
                || vacation.getCategory() == TimeOffRequestType.DIA_DE_LA_MUJER_NACIONAL) {
            daysToBeTakenField.setValue(0.5);
        } else {
            daysToBeTakenField.setValue(daysToBeTaken);
        }
    }

    private double calculateBalanceDays(final double availableDays, final double daysToBeTaken) {
        return availableDays - daysToBeTaken;
    }

    private void clearFields() {
        daysToBeTakenField.clear();
        balanceDaysField.clear();
        endDatePicker.clear();
    }

    private void configureButtons() {
        saveButton = new Button("Guardar", event -> saveRequest());
        closeButton = new Button("Salir", event -> closeForm());
    }

    private void setupFormLayout() {
        add(
                new H3("Añadir solicitud de vacaciones"),
                employeeComboBox,
                categoryComboBox,
                availableDaysField,
                startDatePicker,
                endDatePicker,
                daysToBeTakenField,
                balanceDaysField,
                new HorizontalLayout(saveButton, closeButton)
        );
    }

    private void saveRequest() {
        if (!binder.validate().isOk()) {
            Notification.show("Rellene correctamente todos los campos obligatorios.");
            return;
        }

        if (!validateForm()) {
            Notification.show("Por favor, complete los campos antes de guardar");
            return;
        }

        TimeOffRequest request = prepareRequest();

        if (request.getCategory() == TimeOffRequestType.VACACION_GESTION_ACTUAL) {
            handleVacationRequest(request);
        } else {
            handleExistingRequests(request);
        }

        requestService.saveTimeOffRequest(request);
        Notification.show("Solicitud guardada correctamente.");
        closeForm();
    }

    private TimeOffRequest prepareRequest() {
        TimeOffRequest request = binder.getBean();
        request.setStartDate(startDatePicker.getValue());
        request.setAvailableDays(availableDaysField.getValue());
        request.setExpiration(endDate != null ? endDate : endDatePicker.getValue());
        request.setState(TimeOffRequestStatus.SOLICITADO);
        return request;
    }

    private void handleExistingRequests(final TimeOffRequest request) {
        List<TimeOffRequest> existingRequests =
                requestService.findByEmployeeAndCategory(employee.getId(), request.getCategory());

        int maxRequests = request.getCategory() != TimeOffRequestType.PERMISOS_DE_SALUD
                && !request.getCategory().name().startsWith("VACACION")
                && request.getCategory() != TimeOffRequestType.CUMPLEAÑOS
                ? 2 : 1;

        if (existingRequests.size() >= maxRequests) {
            existingRequests.stream()
                    .min(Comparator.comparing(TimeOffRequest::getStartDate))
                    .ifPresent(oldestRequest -> requestService.deleteTimeOffRequest(oldestRequest.getId()));
        }
    }

    private void handleVacationRequest(final TimeOffRequest request) {
        List<TimeOffRequest> existingRequests = requestService.findByEmployeeAndCategory(
                employee.getId(),
                TimeOffRequestType.VACACION_GESTION_ACTUAL
        );
        if (!existingRequests.isEmpty()) {
            TimeOffRequest existingRequest = existingRequests.getFirst();
            existingRequest.setCategory(TimeOffRequestType.VACACION_GESTION_ANTERIOR);
            requestService.saveTimeOffRequest(existingRequest);
        }
    }

    private boolean validateForm() {
        return employeeComboBox.getValue() != null
                && categoryComboBox.getValue() != null
                && startDatePicker.getValue() != null
                && endDatePicker.getValue() != null;
    }

    private void closeForm() {
        getUI().ifPresent(ui -> ui.navigate(RequestsListView.class));
    }
}
