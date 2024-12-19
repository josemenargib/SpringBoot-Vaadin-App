package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Document;
import com.primefactorsolutions.model.DocumentType;
import com.primefactorsolutions.model.Employee;
import com.primefactorsolutions.service.DocumentService;
import com.primefactorsolutions.service.EmployeeService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UserDetails;
import org.vaadin.firitin.form.BeanValidationForm;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.io.InputStream;

@SpringComponent
@PermitAll
@Scope("prototype")
@PageTitle("Document")
@Route(value = "/documents/:documentId?/:action?", layout = MainLayout.class)
public class DocumentView extends BeanValidationForm<Document> implements HasUrlParameter<String> {
    private final TextField fileName = new TextField("Document Name");
    private final ComboBox<DocumentType> documentType = new ComboBox<>("Document Type");
    private final ComboBox<Employee> employeeComboBox = new ComboBox<>("Employee");
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload uploadButton = new Upload(buffer);
    private final DocumentService documentService;
    private final EmployeeService employeeService;
    private final AuthenticationContext authContext;
    private boolean fileUploaded = false;
    private Button saveButton;
    private Button viewDocumentButton;


    public DocumentView(final DocumentService documentService,
                        final EmployeeService employeeService,
                        final AuthenticationContext authContext) {
        super(Document.class);
        this.documentService = documentService;
        this.employeeService = employeeService;
        this.authContext = authContext;
        initializeView();
    }

    private void initializeView() {
        configureComponents();
        configureUploadButton();
    }

    protected Button createSaveButton() {
        saveButton = new Button("Save");
        saveButton.addClickListener(event -> saveDocument());
        return saveButton;
    }

    protected Button createCloseButton() {
        Button closeButton = new Button("Close");
        closeButton.addClickListener(event -> closeForm());
        return closeButton;
    }

    protected Button createViewDocumentButton() {
        viewDocumentButton = new Button("View Document");
        viewDocumentButton.setEnabled(false);
        viewDocumentButton.addClickListener(event -> viewDocument());
        return viewDocumentButton;
    }

    private void setFileNameProperties() {
        fileName.setWidthFull();
    }

    private void setDocumentTypeProperties() {
        documentType.setItems(DocumentType.values());
        documentType.setWidthFull();
    }

    private void setEmployeeComboBoxProperties() {
        List<Employee> employees = employeeService.findAllEmployees();
        employeeComboBox.setItems(employees);
        employeeComboBox.setItemLabelGenerator(employee -> employee.getFirstName() + " " + employee.getLastName());
        employeeComboBox.setWidthFull();
    }

    private void setDocumentCreator(final Document document) {
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            document.setCreator(user.getUsername());
        });
    }

    private void setFieldsReadOnly(final boolean option) {
        fileName.setReadOnly(option);
        documentType.setReadOnly(option);
        employeeComboBox.setReadOnly(option);
    }

    private void viewDocument() {
        StreamResource resource;
        try {
            InputStream inputStream = buffer.getInputStream();
            if (inputStream != null && inputStream.available() > 0) {
                resource = new StreamResource(fileName.getValue(), () -> new ByteArrayInputStream(readFileData()));
            } else {
                byte[] fileData = getEntity().getFileData();
                resource = new StreamResource(fileName.getValue(), () -> new ByteArrayInputStream(fileData));
            }
            resource.setContentType("application/pdf");
            getUI().ifPresent(ui -> {
                StreamRegistration registration = ui.getSession().getResourceRegistry().registerResource(resource);
                ui.getPage().open(registration.getResourceUri().toString());
            });
        } catch (IOException e) {
            Notification.show("Error reading file.");
        }
    }

    private void navigateToDocumentsListView() {
        getUI().ifPresent(ui -> ui.navigate(DocumentsListView.class));
    }

    private void saveDocument() {
        if (isFormValid()) {
            Document document = getEntity();
            document.setFileName(fileName.getValue());
            document.setDocumentType(documentType.getValue());
            document.setEmployee(employeeComboBox.getValue());
            document.setFileData(readFileData());
            setDocumentCreator(document);

            documentService.saveDocument(document);
            Notification.show("File saved successfully.");
            clearForm();
        } else {
            Notification.show("Save failed: Please complete all fields and upload a file.");
        }
    }

    private void closeForm() {
        navigateToDocumentsListView();
    }

    private boolean isFormValid() {
        return !fileName.isEmpty()
                && documentType.getValue() != null
                && employeeComboBox.getValue() != null
                && fileUploaded;
    }

    private void clearForm() {
        fileName.clear();
        documentType.clear();
        employeeComboBox.clear();
        fileUploaded = false;
        uploadButton.getElement().setPropertyJson("files", Json.createArray());
        viewDocumentButton.setEnabled(false);
    }

    private byte[] readFileData() {
        try {
            return buffer.getInputStream().readAllBytes();
        } catch (IOException e) {
            Notification.show("Error reading file data.");
            return new byte[0];
        }
    }

    private void preLoadFile(final Document document) {
        JsonArray jsonArray = Json.createArray();
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("name", document.getFileName());
        jsonObject.put("progress", 100);
        jsonObject.put("complete", true);
        jsonObject.put("fileData", Base64.getEncoder().encodeToString(document.getFileData()));
        jsonArray.set(0, jsonObject);
        uploadButton.getElement().setPropertyJson("files", jsonArray);
        fileUploaded = true;
    }

    private void updateSaveButtonState() {
        boolean isModified = !fileName.getValue().equals(getEntity().getFileName())
                || documentType.getValue() != getEntity().getDocumentType()
                || employeeComboBox.getValue() != getEntity().getEmployee()
                || fileUploaded;
        saveButton.setEnabled(isModified);
    }

    private void configureComponents() {
        setFileNameProperties();
        setDocumentTypeProperties();
        setEmployeeComboBoxProperties();
        fileName.addValueChangeListener(e -> updateSaveButtonState());
        documentType.addValueChangeListener(e -> updateSaveButtonState());
        employeeComboBox.addValueChangeListener(e -> updateSaveButtonState());
        uploadButton.addSucceededListener(e -> updateSaveButtonState());
        uploadButton.getElement().addEventListener("file-remove", event -> updateSaveButtonState());
    }

    private void configureUploadButton() {
        uploadButton.setMaxFiles(1);
        uploadButton.setAcceptedFileTypes(".pdf");
        uploadButton.addSucceededListener(event -> {
            fileUploaded = true;
            Notification.show("File uploaded successfully.");
            viewDocumentButton.setEnabled(true);
            updateSaveButtonState();
        });
        uploadButton.getElement().addEventListener("file-remove", event -> {
            fileUploaded = false;
            Notification.show("File removed.");
            viewDocumentButton.setEnabled(false);
            updateSaveButtonState();
        });
    }

    private void configureViewOrEditAction(final String action, final String documentIdString) {
        if ("edit".equals(action) && !documentIdString.isEmpty()) {
            setFieldsReadOnly(false);
            preLoadFile(getEntity());
            viewDocumentButton.setEnabled(true);
        } else if ("view".equals(action) && !documentIdString.isEmpty()) {
            setFieldsReadOnly(true);
            preLoadFile(getEntity());
            saveButton.setEnabled(false);
            viewDocumentButton.setEnabled(true);
        }
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String action) {
        final RouteParameters params = beforeEvent.getRouteParameters();
        final String documentIdString = params.get("documentId").orElse(null);

        if ("new".equals(action)) {
            setEntityWithEnabledSave(new Document());
        } else {
            assert documentIdString != null;
            UUID documentId = UUID.fromString(documentIdString);
            Document document = documentService.getDocument(documentId);
            setEntity(document);
            employeeComboBox.setValue(document.getEmployee());
            preLoadFile(document);
            configureViewOrEditAction(action, documentIdString);
        }
    }

    @Override
    protected List<Component> getFormComponents() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.add(uploadButton, createViewDocumentButton());
        buttonLayout.setSpacing(true);
        return List.of(fileName, documentType, employeeComboBox, buttonLayout, createCloseButton());
    }
}
