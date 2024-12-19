package com.primefactorsolutions.views;

import com.primefactorsolutions.model.*;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.service.TeamService;
import com.primefactorsolutions.service.TimeOffRequestService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.form.BeanValidationForm;

import java.util.List;
import java.util.UUID;

@SpringComponent
@PermitAll
@Scope("prototype")
@PageTitle("Request")
@Route(value = "/requests/:requestId?/:action?", layout = MainLayout.class)
public class RequestView extends BeanValidationForm<TimeOffRequest> implements HasUrlParameter<String> {

    private final ComboBox<TimeOffRequestStatus> state = new ComboBox<>("Estado de la solicitud");
    private final DatePicker expiration = new DatePicker("Vencimiento");
    private final DatePicker startDate = new DatePicker("Fecha de inicio");
    private final DatePicker endDate = new DatePicker("Fecha de fin");
    private final NumberField availableDays = new NumberField("Días disponibles");
    private final NumberField daysToBeTake = new NumberField("Días a tomar");

    private final TimeOffRequestService requestService;
    private final EmployeeService employeeService;
    private TimeOffRequest request;
    private Employee employee;

    private Button saveButton;

    public RequestView(final TimeOffRequestService requestService,
                       final EmployeeService employeeService,
                       final TeamService teamService) {
        super(TimeOffRequest.class);
        this.requestService = requestService;
        this.employeeService = employeeService;
        state.setItems(List.of(TimeOffRequestStatus.values()));
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String action) {
        final RouteParameters params = beforeEvent.getRouteParameters();
        final String requestIdString = params.get("requestId").orElse(null);

        if ("new".equals(action)) {
            setEntityWithEnabledSave(new TimeOffRequest());
        } else {
            assert requestIdString != null;
            UUID requestId = UUID.fromString(requestIdString);
            request = requestService.findTimeOffRequest(requestId);
            UUID employeeId = request.getEmployee().getId();
            employee = employeeService.getEmployee(employeeId);
            setEntity(request);
            configureViewOrEditAction(action);
        }
    }

    @Override
    protected List<Component> getFormComponents() {
        return List.of(
                createEmployeeHeader(),
                createTeamHeader(),
                createCategoryHeader(),
                state,
                expiration,
                startDate,
                endDate,
                availableDays,
                daysToBeTake,
                createCloseButton()
        );
    }

    protected Button createSaveButton() {
        saveButton = new Button("Guardar");
        saveButton.addClickListener(event -> saveRequest());
        return saveButton;
    }

    protected Button createCloseButton() {
        Button closeButton = new Button("Salir");
        closeButton.addClickListener(event -> closeForm());
        return closeButton;
    }

    private void setFieldsReadOnly(final boolean option) {
        state.setReadOnly(option);
        expiration.setReadOnly(option);
        startDate.setReadOnly(option);
        endDate.setReadOnly(option);
        availableDays.setReadOnly(option);
        daysToBeTake.setReadOnly(option);
    }

    private void saveRequest() {
        if (isFormValid()) {
            TimeOffRequest request = getEntity();
            setRequestFieldValues(request);
            requestService.saveTimeOffRequest(request);
            Notification.show("Solicitud guardada correctamente.");
            closeForm();
        }
    }

    private void setRequestFieldValues(final TimeOffRequest request) {
        request.setState(state.getValue());
        request.setExpiration(expiration.getValue());
        request.setStartDate(startDate.getValue());
        request.setEndDate(endDate.getValue());
        request.setAvailableDays(availableDays.getValue());
        request.setDaysToBeTake(daysToBeTake.getValue());
    }

    private void closeForm() {
        getUI().ifPresent(ui -> ui.navigate("requests/" + employee.getId().toString()));
    }

    private boolean isFormValid() {
        return !state.isEmpty()
                && expiration.getValue() != null
                && startDate.getValue() != null
                && endDate.getValue() != null
                && availableDays.getValue() != null
                && daysToBeTake.getValue() != null;
    }

    private void configureViewOrEditAction(final String action) {
        if ("edit".equals(action) && !request.getId().toString().isEmpty()) {
            setFieldsReadOnly(false);
        } else if ("view".equals(action) && !request.getId().toString().isEmpty()) {
            setFieldsReadOnly(true);
            saveButton.setEnabled(false);
        }
    }

    private H3 createEmployeeHeader() {
        return new H3("Empleado: " + employee.getFirstName() + " " + employee.getLastName());
    }

    private H3 createTeamHeader() {
        return new H3("Equipo: " + employee.getTeam().getName());
    }

    private H3 createCategoryHeader() {
        return new H3("Categoría: " + request.getCategory());
    }
}
