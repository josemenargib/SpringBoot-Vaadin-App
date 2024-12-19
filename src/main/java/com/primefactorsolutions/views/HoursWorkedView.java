package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Employee;
import com.primefactorsolutions.model.HoursWorked;
import com.primefactorsolutions.model.Team;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.service.HoursWorkedService;
import com.primefactorsolutions.service.TeamService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.components.datepicker.VDatePicker;
import org.vaadin.firitin.form.BeanValidationForm;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringComponent
@PermitAll
@Scope("prototype")
@PageTitle("Horas Trabajadas")
@Route(value = "/hours-worked-list/:hours-workedId?/:action?", layout = MainLayout.class)
public class HoursWorkedView extends BeanValidationForm<HoursWorked> implements HasUrlParameter<String> {
    private final VDatePicker dateField = new VDatePicker("Fecha");
    private final ComboBox<Team> teamField = new ComboBox<>("Equipo");
    private ComboBox<Employee> employeeField;
    private final ComboBox<String> tareasEspecificasDropdown = new ComboBox<>("Tarea Específica");
    private final TextField tareaEspecificaInput = new TextField("Otra Tarea Específica");
    private final TextField horasTareaEspecificaField = new TextField("Horas Tarea Específica");
    private final TextField activityField = new TextField("Actividad");
    private final TextField hoursField = new TextField("Horas");

    private final H2 equipoLabel = new H2("Tareas del Cliente/Equipo");
    private final H2 empresaLabel = new H2("Tareas de la Empresa");
    private final Label totalCompletadoLabel = new Label();

    private final HoursWorkedService hoursWorkedService;
    private final EmployeeService employeeService;
    private final TeamService teamService;
    private HoursWorked hoursWorked;
    private Employee employee;

    private Button saveButton;

    public HoursWorkedView(final HoursWorkedService hoursWorkedService,
                           final EmployeeService employeeService,
                           final TeamService teamService) {
        super(HoursWorked.class);
        this.hoursWorkedService = hoursWorkedService;
        this.employeeService = employeeService;
        this.teamService = teamService;

        initializeDateField();
        initializeTeamField();
        initializeEmployeeField();
        configureTareasEspecificas();

    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String action) {
        final RouteParameters params = beforeEvent.getRouteParameters();
        final String s = params.get("hours-workedId").orElse(null);

        if ("new".equals(action)) {
            setEntityWithEnabledSave(new HoursWorked());
        } else {
            UUID hoursWorkedId = UUID.fromString(s);
            var hoursWorked = hoursWorkedService.getHoursWorked(hoursWorkedId);
            setEntityWithEnabledSave(hoursWorked);

            if ("edit".equals(action) && !s.isEmpty()) {
                saveButton.setVisible(true);
            } else if ("view".equals(action) && !s.isEmpty()) {
                saveButton.setVisible(false);
            }
        }
    }

    @Override
    protected List<Component> getFormComponents() {
        return List.of(
                dateField,
                teamField,
                employeeField,
                equipoLabel,
                activityField,
                hoursField,
                empresaLabel,
                tareasEspecificasDropdown,
                tareaEspecificaInput,
                horasTareaEspecificaField,
                createCloseButton()
        );
    }

    private void configureTareasEspecificas() {
        tareasEspecificasDropdown.setItems("Entrevistas", "Reuniones",
                "Colaboraciones", "Aprendizajes", "Proyectos PFS",
                "Consulta Medica", "Afiliación al Seguro", "Fallas Tecnicas", "Otros");
        tareasEspecificasDropdown.setPlaceholder("Selecciona una tarea...");

        tareasEspecificasDropdown.addValueChangeListener(event -> {
            String selected = event.getValue();
            boolean isOtros = "Otros".equals(selected);
            tareaEspecificaInput.setVisible(isOtros);
            horasTareaEspecificaField.setVisible(true);
            if (!isOtros) {
                tareaEspecificaInput.clear();
                horasTareaEspecificaField.clear();
            }
        });
        tareaEspecificaInput.setVisible(false);
        horasTareaEspecificaField.setVisible(false);
    }

    protected Button createSaveButton() {
        saveButton = new Button("Guardar");
        saveButton.addClickListener(event -> saveHoursWorked());
        return saveButton;
    }

    protected Button createCloseButton() {
        Button closeButton = new Button("Cerrar");
        closeButton.addClickListener(event -> closeForm());
        return closeButton;
    }

    private void initializeTeamField() {
        List<Team> teams = new ArrayList<>(teamService.findAllTeams());
        teamField.setItems(teamService.findAllTeams());
        teamField.setItemLabelGenerator(Team::getName);
        teamField.setValue(teams.getFirst());
        teamField.addValueChangeListener(event -> {
                    if (teams != null) {
                        employeeField.getValue();
                        event.getValue();
                    }
                }
        );
    }

    private ComboBox<Employee> initializeEmployeeField() {
        employeeField = new ComboBox<>("Empleado");
        List<Employee> employees = new ArrayList<>(employeeService.findAllEmployees());
        employeeField.setItems(employees);
        employeeField.setItemLabelGenerator(this::getEmployeeFullName);
        employeeField.setValue(employees.getFirst());
        return employeeField;
    }

    private String getEmployeeFullName(final Employee employee) {
        return "TODOS".equals(employee.getFirstName())
                ? "TODOS" : employee.getFirstName() + " " + employee.getLastName();
    }

    private void initializeDateField() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.of(today.getYear(), today.getMonth());

        LocalDate startOfMonth = currentMonth.atDay(1);

        LocalDate maxSelectableDate = today;

        dateField.setMin(startOfMonth);
        dateField.setMax(maxSelectableDate);
        dateField.setValue(today);

        dateField.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            if (selectedDate != null) {
                int weekNumber = selectedDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                Notification.show("Número de la semana: " + weekNumber,
                        3000, Notification.Position.BOTTOM_CENTER);
                if (hoursWorked != null) {
                    hoursWorked.setWeekNumber(weekNumber);
                }
            }
        });
    }

    private void saveHoursWorked() {
        if (isFormValid()) {
            HoursWorked hoursWorked = getEntity();
            String actividad = activityField.getValue();
            String tareaEspecifica = tareasEspecificasDropdown.getValue();
            if (actividad != null && !actividad.isEmpty() && tareaEspecifica != null) {
                Notification.show("Solo puedes elegir una: actividad del proyecto o tarea de la empresa.",
                        3000, Notification.Position.BOTTOM_CENTER);
                return;
            }
            if (actividad != null && !actividad.isEmpty()) {
                hoursWorked.setActividad(actividad);
            } else if (tareaEspecifica != null) {
                if ("Otros".equals(tareaEspecifica)) {
                    // Validar que se ingresó una tarea específica en el campo de texto
                    String tareaEspecificaInputValue = tareaEspecificaInput.getValue();
                    if (tareaEspecificaInputValue == null || tareaEspecificaInputValue.isEmpty()) {
                        Notification.show("Por favor, ingresa una tarea específica.",
                                3000, Notification.Position.BOTTOM_CENTER);
                        return;
                    }
                    hoursWorked.setTareaEspecifica(tareaEspecificaInputValue);
                } else {
                    hoursWorked.setTareaEspecifica(tareaEspecifica);
                }
            } else {
                Notification.show("Por favor, selecciona una actividad o tarea para guardar.",
                        3000, Notification.Position.BOTTOM_CENTER);
                return;
            }
            setFieldValues(hoursWorked);
            hoursWorkedService.save(hoursWorked);
            Notification.show("Horas trabajadas guardadas correctamente.",
                    3000, Notification.Position.BOTTOM_CENTER);
            closeForm();
        }
    }



    private void setFieldValues(final HoursWorked hoursWorked) {
        hoursWorked.setDate(dateField.getValue());
        hoursWorked.setTeam(teamField.getValue());
        hoursWorked.setEmployee(employeeField.getValue());
        hoursWorked.setActividad(activityField.getValue());
        try {
            double hours = Double.parseDouble(hoursField.getValue());
            hoursWorked.setHours(hours);
        } catch (NumberFormatException e) {
            Notification.show("Por favor, ingrese un número válido para las horas.");
        }
        if ("Otros".equals(tareasEspecificasDropdown.getValue())) {
            hoursWorked.setActividad(tareaEspecificaInput.getValue());
            try {
                double horasEspecifica = Double.parseDouble(horasTareaEspecificaField.getValue());
                hoursWorked.setHours(horasEspecifica);
                double totalHoras = hoursWorked.getHours() + horasEspecifica;
                hoursWorked.setTotalHours(totalHoras);
            } catch (NumberFormatException e) {
                Notification.show("Por favor, ingrese un número válido para las horas de la tarea específica.");
            }
        }
    }

    private void closeForm() {
        if (hoursWorked != null) {
            getUI().ifPresent(ui -> ui.navigate("hours-worked-list/" + hoursWorked.getId().toString()));
        } else {
            getUI().ifPresent(ui -> ui.navigate("hours-worked-list"));
        }
    }

    private boolean isFormValid() {
        boolean isTareaEspecificaValida = "Otros".equals(tareasEspecificasDropdown.getValue())
                ? !tareaEspecificaInput.isEmpty()
                : tareasEspecificasDropdown.getValue() != null;
        boolean isActividadValida = !activityField.isEmpty();
        boolean isSoloUnaOpcionElegida = (isActividadValida && tareasEspecificasDropdown.isEmpty())
                || (!isActividadValida && isTareaEspecificaValida);
        return dateField.getValue() != null
                && teamField.getValue() != null
                && employeeField.getValue() != null
                && isSoloUnaOpcionElegida;
    }


    private void configureViewOrEditAction(final String action) {
        if ("edit".equals(action) && hoursWorked != null) {
            setFieldsReadOnly(false);
        } else if ("view".equals(action) && hoursWorked != null) {
            setFieldsReadOnly(true);
            saveButton.setEnabled(false);
        }
    }

    private void setFieldsReadOnly(final boolean readOnly) {
        dateField.setReadOnly(readOnly);
        teamField.setReadOnly(readOnly);
        employeeField.setReadOnly(readOnly);
        activityField.setReadOnly(readOnly);
    }
}