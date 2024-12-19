package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Employee;
import com.primefactorsolutions.model.Team;
import com.primefactorsolutions.model.*;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.service.ReportService;
import com.primefactorsolutions.service.TeamService;
import com.primefactorsolutions.service.TimeOffRequestService;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.components.datepicker.VDatePicker;
import org.vaadin.firitin.form.BeanValidationForm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@SpringComponent
@PermitAll
@Scope("prototype")
@PageTitle("Employee")
@Route(value = "/employees/:employeeId?/:action?", layout = MainLayout.class)
public class EmployeeView extends BeanValidationForm<Employee> implements HasUrlParameter<String> {
    private final EmployeeService employeeService;
    private final ReportService reportService;
    private final TimeOffRequestService requestService;
    private final TeamService teamService;
    // TODO: campo usado para registrar al empleado en LDAP. Este campo podria estar en otro form eventualmente.
    private final TextField username = createTextField("Username: ", 30, true);
    private final TextField firstName = createTextField("Nombres: ", 30, true);
    private final TextField lastName = createTextField("Apellidos", 30, true);
    private final ComboBox<Employee.Status> status = createStatusComboBox();
    private final ComboBox<Employee.Gender> gender = createGenderComboBox();
    private final VDatePicker birthday = new VDatePicker("Fecha de Nacimiento");
    private final TextField age = createTextField("Edad", 3, false);
    private final TextField birthCity = createTextField("Ciudad y País de Nacimiento     ejemplo: (Ciudad, País) ",
            30, false);
    private final TextField residenceAddress = createTextField("Dirección de Residencia", 50, false);
    private final TextField localAddress = createTextField("Departamento y Provincia de Residencia   "
            + "  ejemplo: (Departamento-Provincia)", 30, false);
    private final ComboBox<Employee.MaritalStatus> maritalStatus = createMaritalStatusComboBox();
    private final TextField numberOfChildren = createTextField("Numero de Hijos", 1, false);
    private final TextField ci = createTextField("CI", 10, false);
    private final TextField issuedIn = createTextField("Expedido en ", 10, false);
    private final TextField phoneNumber = createTextField("Teléfono", 8, false);
    private final EmailField personalEmail = createEmailField("E-mail      ejemplo: (ejemplo@gmail.com)");
    private final TextField phoneNumberProfesional = createTextField("Teléfono Laboral", 8, false);
    private final EmailField profesionalEmail = createEmailField("E-mail Laboral     ejemplo: "
            + "(ejemplo@primerfactorsolutions.com)");
    private final TextField emergencyCName = createTextField("Nombres y Apellidos de Contacto", 50, false);
    private final TextField emergencyCAddress = createTextField("Dirección de Contacto", 50, false);
    private final TextField emergencyCPhone = createTextField("Teléfono de Contacto", 8, false);
    private final EmailField emergencyCEmail = createEmailField("Email de Contacto     ejemplo: (ejemplo@gmail.com)");
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);
    private final Image profileImagePreview = new Image();
    private final TextField pTitle1 =  createTextField("Título 1", 30, false);
    private final TextField pTitle2 =  createTextField("Título 2", 30, false);
    private final TextField pTitle3 =  createTextField("Título 3", 30, false);
    private final TextField pStudy1 =  createTextField("Estudio 1", 30, false);
    private final TextField pStudy2 =  createTextField("Estudio 2", 30, false);
    private final TextField pStudy3 =  createTextField("Estudio 3", 30, false);
    private final TextField certification1 =  createTextField("Certificación 1", 30, false);
    private final TextField certification2 =  createTextField("Certificación 2", 30, false);
    private final TextField certification3 =  createTextField("Certificación 3", 30, false);
    private final TextField certification4 =  createTextField("Certificación 4", 30, false);
    private final TextField recognition =  createTextField("Reconocimientos", 30, false);
    private final TextField achievements =  createTextField("Logros Profesionales", 30, false);
    private final TextField language1 =  createTextField("Idioma 1", 30, false);
    private final TextField language1Level =  createTextField("Nivel de Idioma", 30, false);
    private final TextField language2 =  createTextField("Idioma 2", 30, false);
    private final TextField language2Level =  createTextField("Nivel de Idioma", 30, false);
    private final TextField cod =  createTextField("Codigo de Empleado", 20, false);
    private final TextField position = createTextField("Cargo", 30, false);
    private final ComboBox<Team> team = new ComboBox<>("Equipo");
    private final TextField leadManager =  createTextField("Lead/Manager", 30, false);
    private final VDatePicker dateOfEntry = new VDatePicker("Fecha de Ingreso");
    private final VDatePicker dateOfExit = new VDatePicker("Fecha de Retiro");
    private final ComboBox<Employee.ContractType> contractType =  createContractTypeComboBox();
    private final TextField seniority =  createTextField("Antiguedad", 30, false);
    private final TextField salaryTotal =  createTextField("Salario Total", 10, false);
    private final TextField salaryBasic = createTextField("Salario Basico", 10, false);
    private final TextField antiguedad = createTextField("Descuento por Antiguedad", 10, false);
    private final TextField bonoProfesional = createTextField("Bono Profesional", 30, false);
    private final TextField bankName =  createTextField("Banco", 30, false);
    private final TextField accountNumber =  createTextField("Nro. de Cuenta", 30, false);
    private final TextField gpss =  createTextField("Código Único de Asegurado (GPSS)", 30, false);
    private final TextField sss =  createTextField("Matricula de Asegurado (SSS)", 30, false);
    private final TextField beneficiarie1 =  createTextField("Derechohabiente 1", 30, false);
    private final TextField beneficiarie2 =  createTextField("Derechohabiente 2", 30, false);
    private static final String SAVE_BUTTON_TEXT = "Save";
    private static final String EDIT_BUTTON_TEXT = "Edit";
    private static final String NOTIFICATION_SAVE_SUCCESS = "Employee saved successfully.";
    private static final String NOTIFICATION_VALIDATE_ERROR = "Please complete the required fields correctly.";
    private static final String PHONE_NUMBER_ERROR_MESSAGE = "El teléfono debe contener solo números.";
    private final Button saveButton = new Button(SAVE_BUTTON_TEXT, e -> saveEmployee());
    private final Button editButton = new Button(EDIT_BUTTON_TEXT, e -> enableEditMode());
    private final Button reportButton = new Button("Generar Ficha");
    private final Dialog dialog = new Dialog();
    private final PdfViewer pdfViewer = new PdfViewer();
    private final H2 infoPer = new H2("Información Personal");
    private final H3 infoGenr = new H3("Información General");
    private final H3 contEmerg = new H3("Contacto de Emergencia");
    private final H2 infProf = new H2("Información Profesional");
    private final H3 titulos = new H3("Titulos Profesionales y Estudios Realizados");
    private final H3 certif = new H3("Certificaciones Profesionales");
    private final H3 logros = new H3("Otros Logros y Reconocimientos");
    private final H3 idioma = new H3("Dominio de Idiomas");
    private final H2 infoAdm = new H2("Información Administrativa");
    private final H3 infoCont = new H3("Información de Contratación");
    private final H3 datBanc = new H3("Datos Bancarios");
    private final H3 datGest = new H3("Datos Gestora Pública y Seguro Social");

    public EmployeeView(final EmployeeService employeeService,
                        final ReportService reportService,
                        final TeamService teamService,
                        final TimeOffRequestService requestService) {
        super(Employee.class);
        this.employeeService = employeeService;
        this.reportService = reportService;
        this.requestService = requestService;
        this.teamService = teamService;
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        configureComponents();
        addClassName("main-layout");
    }

    private void makeUpperCase(final TextField textField) {
        textField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value != null) {
                textField.setValue(value.toUpperCase());
            }
        });
    }

    private void configureComponents() {
        phoneNumber.setValueChangeMode(ValueChangeMode.EAGER);
        phoneNumber.addValueChangeListener(e -> validatePhoneNumber(phoneNumber, e.getValue()));
        emergencyCPhone.setValueChangeMode(ValueChangeMode.EAGER);
        emergencyCPhone.addValueChangeListener(e -> validatePhoneNumber(emergencyCPhone, e.getValue()));
        firstName.setValueChangeMode(ValueChangeMode.EAGER);
        firstName.addValueChangeListener(e -> validateNameField(firstName, e.getValue()));
        lastName.setValueChangeMode(ValueChangeMode.EAGER);
        lastName.addValueChangeListener(e -> validateNameField(lastName, e.getValue()));
        createTeamComboBox();
        configureUpload();
        saveButton.setVisible(true);
        editButton.setVisible(true);
        reportButton.setVisible(true);
        birthday.addValueChangeListener(event -> calculateAge());
        birthday.setMax(java.time.LocalDate.now().minusYears(18));
        salaryTotal.addValueChangeListener(event -> calculateSalaryTotal());
        dateOfEntry.addValueChangeListener(event -> calculateSeniority());
        dateOfExit.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                status.setValue(Employee.Status.INACTIVE);
            } else {
                status.setValue(Employee.Status.ACTIVE);
            }
        });
        reportButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            var employee = getEntity();
            byte[] pdfContent = reportService.writeAsPdf("ficha", employee);
            var resource = new StreamResource("ficha.pdf", () -> new ByteArrayInputStream(pdfContent));
            pdfViewer.setSrc(resource);
            dialog.open();
        });

        makeUpperCase(firstName);
        makeUpperCase(lastName);
        makeUpperCase(birthCity);
        makeUpperCase(residenceAddress);
        makeUpperCase(localAddress);
        makeUpperCase(position);
        makeUpperCase(emergencyCName);
        makeUpperCase(emergencyCAddress);
        makeUpperCase(ci);
        makeUpperCase(issuedIn);
        makeUpperCase(pTitle1);
        makeUpperCase(pTitle2);
        makeUpperCase(pTitle3);
        makeUpperCase(pStudy1);
        makeUpperCase(pStudy2);
        makeUpperCase(pStudy3);
        makeUpperCase(certification1);
        makeUpperCase(certification2);
        makeUpperCase(certification3);
        makeUpperCase(certification4);
        makeUpperCase(recognition);
        makeUpperCase(achievements);
        makeUpperCase(language1);
        makeUpperCase(language1Level);
        makeUpperCase(language2);
        makeUpperCase(language2Level);
        makeUpperCase(cod);
        makeUpperCase(leadManager);
        makeUpperCase(seniority);
        makeUpperCase(bankName);
        makeUpperCase(accountNumber);
        makeUpperCase(gpss);
        makeUpperCase(sss);
        makeUpperCase(beneficiarie1);
        makeUpperCase(beneficiarie2);
        initDialog();
    }

    private void validateNameField(final TextField textField, final String value) {
        if (!value.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$")) {
            textField.setInvalid(true);
            textField.setErrorMessage("Este campo solo debe contener letras.");
        } else {
            textField.setInvalid(false);
        }
    }

    private void calculateAge() {
        if (birthday.getValue() != null) {
            int currentYear = java.time.LocalDate.now().getYear();
            int birthYear = birthday.getValue().getYear();
            int ages = currentYear - birthYear;
            age.setValue(String.valueOf(ages));
            birthday.setInvalid(ages < 18);
            System.out.println(age);
        }
    }

    private void calculateSeniority() {
        LocalDate entryDate = dateOfEntry.getValue();
        LocalDate exitDate = dateOfExit.getValue() != null ? dateOfExit.getValue() : LocalDate.now();
        if (entryDate != null) {
            long yearsOfService = ChronoUnit.YEARS.between(entryDate, exitDate);
            String seniorityValue = yearsOfService + " años ";
            seniority.setValue(seniorityValue);
        } else {
            seniority.setValue("No disponible");
        }
    }

    private void calculateSalaryTotal() {
        if (contractType.getValue() == Employee.ContractType.CONTRATO_LABORAL) {
            salaryBasic.setVisible(true);
            bonoProfesional.setVisible(true);
            antiguedad.setVisible(true);
            salaryTotal.setVisible(true);
            salaryBasic.addValueChangeListener(event -> updateTotalSalary());
            bonoProfesional.addValueChangeListener(event -> updateTotalSalary());
            antiguedad.addValueChangeListener(event -> updateTotalSalary());
        } else {
            salaryBasic.setVisible(false);
            bonoProfesional.setVisible(false);
            antiguedad.setVisible(false);
            salaryTotal.setVisible(true);
        }
        salaryTotal.getValue();
    }

    private void updateTotalSalary() {
        try {
            double basic = parseDoubleValue(salaryBasic.getValue());
            double bonus = parseDoubleValue(bonoProfesional.getValue());
            double seniorityBonus = parseDoubleValue(antiguedad.getValue());
            double totalSalary = basic + bonus + seniorityBonus;
            salaryTotal.setValue(String.valueOf(totalSalary));
        } catch (Exception e) {
            salaryTotal.setValue("0.0");
        }
    }

    private double parseDoubleValue(final String value) {
        try {
            return value != null && !value.isEmpty() ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void configureUpload() {
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFileSize(1024 * 1024);
        upload.addSucceededListener(event -> {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                buffer.getInputStream().transferTo(outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                getEntity().setProfileImage(base64Image);
                profileImagePreview.setSrc("data:image/png;base64," + base64Image);
                profileImagePreview.setMaxWidth("150px");
                profileImagePreview.setMaxHeight("150px");
            } catch (IOException e) {
                Notification.show("Error al subir la imagen: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Notification.show("Error en el servidor al procesar la imagen.");
                e.printStackTrace();
            }
        });
    }

    private void validatePhoneNumber(final TextField textField, final String value) {
        if (!value.matches("\\d*")) {
            textField.setErrorMessage(PHONE_NUMBER_ERROR_MESSAGE);
        }
    }

    private void initDialog() {
        pdfViewer.setSizeFull();
        H2 headline = new H2("Ficha Empleado");
        headline.getStyle().set("margin", "var(--lumo-space-m) 0 0 0")
                .set("font-size", "1.5em").set("font-weight", "bold");
        final Button cancelDialogButton = new Button("Close", e -> dialog.close());
        final HorizontalLayout buttonLayout = new HorizontalLayout(cancelDialogButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        final VerticalLayout dialogLayout = new VerticalLayout(headline, pdfViewer, buttonLayout);
        dialogLayout.getStyle().set("height", "100%");
        dialogLayout.getStyle().set("overflow", "hidden");
        dialogLayout.getStyle().set("display", "flex");
        dialogLayout.getStyle().set("flex-direction", "column");
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "700px").set("max-width", "100%");
        dialogLayout.getStyle().set("height", "800px").set("max-height", "100%");
        dialog.add(dialogLayout);
    }

    private ComboBox<Employee.MaritalStatus> createMaritalStatusComboBox() {
        ComboBox<Employee.MaritalStatus> comboBox = new ComboBox<>("Estado Civil");
        comboBox.setItems(Employee.MaritalStatus.values());
        comboBox.setItemLabelGenerator(Employee.MaritalStatus::name);
        return comboBox;
    }

    private ComboBox<Employee.Status> createStatusComboBox() {
        ComboBox<Employee.Status> comboBox = new ComboBox<>("Estado");
        comboBox.setItems(Employee.Status.values());
        comboBox.setItemLabelGenerator(Employee.Status::name);
        comboBox.setRequiredIndicatorVisible(true);
        return comboBox;
    }

    private ComboBox<Employee.ContractType> createContractTypeComboBox() {
        ComboBox<Employee.ContractType> comboBox = new ComboBox<>("Tipo de Contrato");
        comboBox.setItems(Employee.ContractType.values());
        comboBox.setItemLabelGenerator(Employee.ContractType::name);
        comboBox.setRequiredIndicatorVisible(true);
        comboBox.setWidth("300px");
        comboBox.setMinWidth("200px");
        return comboBox;
    }

    private VerticalLayout createContentLayout() {
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        return contentLayout;
    }

    private TextField createTextField(final String label, final int maxLength, final boolean required) {
        TextField textField = new TextField(label);
        textField.setWidthFull();
        textField.setMaxLength(maxLength);
        textField.setRequired(required);
        return textField;
    }

    private EmailField createEmailField(final String label) {
        EmailField emailField = new EmailField(label);
        emailField.setWidthFull();
        emailField.setMaxLength(50);
        return emailField;
    }

    private void createTeamComboBox() {
        List<Team> teams = teamService.findAllTeams();
        team.setItems(teams);
        team.setItemLabelGenerator(Team::getName);
        team.setWidthFull();
    }

    private <T> ComboBox<T> createComboBox(final String label, final T[] items) {
        ComboBox<T> comboBox = new ComboBox<>(label);
        comboBox.setItems(items);
        comboBox.setItemLabelGenerator(Object::toString);
        comboBox.setWidthFull();
        return comboBox;
    }

    private ComboBox<Employee.Gender> createGenderComboBox() {
        ComboBox<Employee.Gender> comboBox = new ComboBox<>("Genero");
        comboBox.setItems(Employee.Gender.values());
        comboBox.setItemLabelGenerator(Employee.Gender::name);
        comboBox.setRequiredIndicatorVisible(true);
        return comboBox;
    }

    private boolean validateForm() {
        return !firstName.isEmpty() && !lastName.isEmpty() && status.getValue() != null;
    }

    private void setVacationDuration(
            final Employee employee,
            final TimeOffRequest request,
            final LocalDate referenceDate) {
        double yearsOfService = ChronoUnit.YEARS.between(employee.getDateOfEntry(), referenceDate);
        request.setAvailableDays(calculateAvailableDays(yearsOfService));
    }

    private double calculateAvailableDays(final double yearsOfService) {
        if (yearsOfService > 10) {
            return 30.0;
        } else if (yearsOfService > 5) {
            return 20.0;
        } else if (yearsOfService > 1) {
            return 15.0;
        } else {
            return 0.0;
        }
    }

    private void saveEmployee() {
        if (validateForm()) {
            Employee employee = getEntity();
            employee.setStatus(status.getValue());
            employee.setAge(age.getValue());
            employee.setSalaryBasic(salaryBasic.getValue());
            employee.setBonoProfesional(bonoProfesional.getValue());
            employee.setAntiguedad(antiguedad.getValue());
            employee.setSalarytotal((salaryTotal.getValue()));
            employeeService.createOrUpdate(employee);
            Notification.show(NOTIFICATION_SAVE_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate(EmployeesListView.class));
        } else {
            Notification.show(NOTIFICATION_VALIDATE_ERROR, 3000, Notification.Position.MIDDLE);
        }
    }

    private void enableEditMode() {
        setFieldsEditable();
        saveButton.setVisible(true);
        editButton.setVisible(false);
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String action) {
        final RouteParameters params = beforeEvent.getRouteParameters();
        final String s = params.get("employeeId").orElse(null);
        if ("new".equals(action)) {
            setEntityWithEnabledSave(new Employee());
            saveButton.setVisible(true);
            editButton.setVisible(false);
            setFieldsEditable();
            upload.setVisible(true);
            salaryTotal.setValue(String.valueOf(true));
        } else {
            UUID employeeId = UUID.fromString(s);
            var employee = employeeService.getEmployee(employeeId);
            setEntityWithEnabledSave(employee);
            if ("edit".equals(action) && !s.isEmpty()) {
                saveButton.setVisible(true);
                editButton.setVisible(false);
                status.setValue(employee.getStatus());
                setFieldsEditable();
                upload.setVisible(true);
                displayProfileImage(employee);
                salaryTotal.setValue(employee.getSalarytotal());
            } else if ("view".equals(action) && !s.isEmpty()) {
                setFieldsReadOnly();
                saveButton.setVisible(false);
                editButton.setVisible(true);
                setFieldsReadOnly();
                displayProfileImage(employee);
                upload.setVisible(true);
                salaryTotal.setValue(employee.getSalarytotal());
            }
        }
    }

    private void displayProfileImage(final Employee employee) {
        if (employee.getProfileImage() != null && !employee.getProfileImage().isEmpty()) {
            profileImagePreview.setSrc("data:image/jpeg;base64," + employee.getProfileImage());
            profileImagePreview.setVisible(true);
            profileImagePreview.setMaxWidth("250px");
            profileImagePreview.setMaxHeight("250px");
            upload.setVisible(true);
        } else {
            profileImagePreview.setVisible(true);
            upload.setVisible(true);
        }
    }

    private void setFieldsReadOnly() {
        username.setReadOnly(true);
        firstName.setReadOnly(true);
        lastName.setReadOnly(true);
        status.setReadOnly(true);
        birthday.setReadOnly(true);
        birthCity.setReadOnly(true);
        residenceAddress.setReadOnly(true);
        localAddress.setReadOnly(true);
        maritalStatus.setReadOnly(true);
        numberOfChildren.setReadOnly(true);
        phoneNumber.setReadOnly(true);
        personalEmail.setReadOnly(true);
        phoneNumberProfesional.setReadOnly(true);
        profesionalEmail.setReadOnly(true);
        position.setReadOnly(true);
        team.setReadOnly(true);
        emergencyCName.setReadOnly(true);
        emergencyCAddress.setReadOnly(true);
        emergencyCPhone.setReadOnly(true);
        emergencyCEmail.setReadOnly(true);
        upload.setVisible(true);
        profileImagePreview.setVisible(true);
        age.setReadOnly(true);
        gender.setReadOnly(true);
        status.setReadOnly(true);
        ci.setReadOnly(true);
        issuedIn.setReadOnly(true);
        pTitle1.setReadOnly(true);
        pTitle2.setReadOnly(true);
        pTitle3.setReadOnly(true);
        pStudy1.setReadOnly(true);
        pStudy2.setReadOnly(true);
        pStudy3.setReadOnly(true);
        certification1.setReadOnly(true);
        certification2.setReadOnly(true);
        certification3.setReadOnly(true);
        certification4.setReadOnly(true);
        recognition.setReadOnly(true);
        achievements.setReadOnly(true);
        language1.setReadOnly(true);
        language1Level.setReadOnly(true);
        language2.setReadOnly(true);
        language2Level.setReadOnly(true);
        cod.setReadOnly(true);
        leadManager.setReadOnly(true);
        dateOfEntry.setReadOnly(true);
        dateOfExit.setReadOnly(true);
        contractType.setReadOnly(true);
        seniority.setReadOnly(true);
        salaryTotal.setReadOnly(true);
        salaryBasic.setReadOnly(true);
        bonoProfesional.setReadOnly(true);
        antiguedad.setReadOnly(true);
        bankName.setReadOnly(true);
        accountNumber.setReadOnly(true);
        gpss.setReadOnly(true);
        sss.setReadOnly(true);
        beneficiarie1.setReadOnly(true);
        beneficiarie2.setReadOnly(true);
    }

    private void setFieldsEditable() {
        username.setReadOnly(false);
        firstName.setReadOnly(false);
        lastName.setReadOnly(false);
        status.setReadOnly(false);
        birthday.setReadOnly(false);
        birthCity.setReadOnly(false);
        residenceAddress.setReadOnly(false);
        localAddress.setReadOnly(false);
        maritalStatus.setReadOnly(false);
        numberOfChildren.setReadOnly(false);
        phoneNumber.setReadOnly(false);
        personalEmail.setReadOnly(false);
        phoneNumberProfesional.setReadOnly(false);
        profesionalEmail.setReadOnly(false);
        position.setReadOnly(false);
        team.setReadOnly(false);
        emergencyCName.setReadOnly(false);
        emergencyCAddress.setReadOnly(false);
        emergencyCPhone.setReadOnly(false);
        emergencyCEmail.setReadOnly(false);
        upload.setVisible(false);
        profileImagePreview.setVisible(true);
        age.setReadOnly(false);
        gender.setReadOnly(false);
        status.setReadOnly(false);
        ci.setReadOnly(false);
        issuedIn.setReadOnly(false);
        pTitle1.setReadOnly(false);
        pTitle2.setReadOnly(false);
        pTitle3.setReadOnly(false);
        pStudy1.setReadOnly(false);
        pStudy2.setReadOnly(false);
        pStudy3.setReadOnly(false);
        certification1.setReadOnly(false);
        certification2.setReadOnly(false);
        certification3.setReadOnly(false);
        certification4.setReadOnly(false);
        recognition.setReadOnly(false);
        achievements.setReadOnly(false);
        language1.setReadOnly(false);
        language1Level.setReadOnly(false);
        language2.setReadOnly(false);
        language2Level.setReadOnly(false);
        cod.setReadOnly(false);
        leadManager.setReadOnly(false);
        dateOfEntry.setReadOnly(false);
        dateOfExit.setReadOnly(false);
        contractType.setReadOnly(false);
        seniority.setReadOnly(false);
        salaryTotal.setReadOnly(false);
        salaryBasic.setReadOnly(false);
        bonoProfesional.setReadOnly(false);
        antiguedad.setReadOnly(false);
        bankName.setReadOnly(false);
        accountNumber.setReadOnly(false);
        gpss.setReadOnly(false);
        sss.setReadOnly(false);
        beneficiarie1.setReadOnly(false);
        beneficiarie2.setReadOnly(false);
    }

    @Override
    protected List<Component> getFormComponents() {
        return List.of(
                username,
                infoPer,
                infoGenr,
                upload, profileImagePreview,
                firstName, lastName,
                gender, status,
                birthday, age,
                birthCity, residenceAddress, localAddress,
                maritalStatus, ci, issuedIn, numberOfChildren,
                phoneNumber, personalEmail, phoneNumberProfesional, profesionalEmail,
                contEmerg, emergencyCName, emergencyCAddress, emergencyCPhone, emergencyCEmail,
                infProf,
                titulos, pTitle1, pTitle2, pTitle3, pStudy1, pStudy2, pStudy3,
                certif, certification1, certification2, certification3, certification4,
                logros, recognition, achievements,
                idioma, language1, language1Level, language2, language2Level,
                infoAdm,
                cod, position, team, leadManager,
                infoCont, dateOfEntry, dateOfExit, contractType, seniority,
                salaryBasic, bonoProfesional, antiguedad, salaryTotal,
                datBanc, bankName, accountNumber,
                datGest, gpss, sss, beneficiarie1, beneficiarie2,
                saveButton, editButton, reportButton, dialog
        );
    }
}