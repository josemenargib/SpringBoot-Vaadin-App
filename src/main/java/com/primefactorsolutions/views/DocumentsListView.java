package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Document;
import com.primefactorsolutions.model.DocumentType;
import com.primefactorsolutions.model.Employee;
import com.primefactorsolutions.service.DocumentService;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.views.util.MenuBarUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.components.grid.PagingGrid;

import java.io.ByteArrayInputStream;
import java.util.List;

import static com.primefactorsolutions.views.Constants.PAGE_SIZE;

@SpringComponent
@Scope("prototype")
@PageTitle("Documents")
@Route(value = "/documents", layout = MainLayout.class)
@PermitAll
public class DocumentsListView extends BaseView {

    private final DocumentService documentService;
    private final EmployeeService employeeService;
    private final PagingGrid<Document> documentGrid = new PagingGrid<>(Document.class);
    private ComboBox<Employee> employeeFilter;
    private ComboBox<DocumentType> documentTypeFilter;

    public DocumentsListView(final DocumentService documentService, final EmployeeService employeeService) {
        this.documentService = documentService;
        this.employeeService = employeeService;
        initializeView();
        updateDocumentGrid(null, null);
    }

    private void initializeView() {
        getCurrentPageLayout().add(createActionButton("Add Document", this::navigateToAddDocumentView));

        final HorizontalLayout hl = new HorizontalLayout();
        hl.add(createDocumentTypeFilter());
        hl.add(createEmployeeFilter());

        getCurrentPageLayout().add(hl);

        configureDocumentGrid();
        getCurrentPageLayout().add(documentGrid);
    }

    private void configureDocumentGrid() {
        documentGrid.setColumns("fileName", "documentType", "creator");
        documentGrid.addComponentColumn(this::createEmployeeSpan).setHeader("Employee");
        addActionColumns();
        configurePagination();
    }

    private Span createEmployeeSpan(final Document document) {
        Employee employee = document.getEmployee();
        String employeeName = employee.getFirstName() + " " + employee.getLastName();
        return new Span(employeeName);
    }

    private void addActionColumns() {
        documentGrid.addComponentColumn((ValueProvider<Document, Component>) document -> {
            final MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
            final MenuItem viewItem = MenuBarUtils.createIconItem(menuBar, VaadinIcon.EYE, "View");
            viewItem.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent ->
                    navigateToDocumentView(document));
            final MenuItem editItem = MenuBarUtils.createIconItem(menuBar, VaadinIcon.PENCIL, "Edit");
            editItem.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent ->
                    navigateToEditDocumentView(document));
            final MenuItem downloadItem = MenuBarUtils.createIconItem(menuBar, VaadinIcon.DOWNLOAD, "Download");
            downloadItem.addClickListener((ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent ->
                    downloadDocument(document));
            return menuBar;
        });
    }

    private Button createActionButton(final String label, final Runnable onClickAction) {
        Button actionButton = new Button(label);
        actionButton.addClickListener(event -> onClickAction.run());
        return actionButton;
    }

    private ComboBox<DocumentType> createDocumentTypeFilter() {
        documentTypeFilter = new ComboBox<>("Document Type");
        documentTypeFilter.setItems(DocumentType.values());
        documentTypeFilter.setValue(DocumentType.values()[0]);
        documentTypeFilter.addValueChangeListener(event -> {
            updateDocumentGrid(event.getValue(), employeeFilter.getValue());
        });
        return documentTypeFilter;
    }

    private ComboBox<Employee> createEmployeeFilter() {
        employeeFilter = new ComboBox<>("Employee");
        List<Employee> employees = employeeService.findAllEmployees();
        employees.addFirst(createAllEmployeesOption());
        employeeFilter.setItems(employees);
        employeeFilter.setItemLabelGenerator(this::getEmployeeLabel);
        employeeFilter.setValue(employees.getFirst());
        employeeFilter.addValueChangeListener(event -> {
            updateDocumentGrid(documentTypeFilter.getValue(), event.getValue());
        });
        return employeeFilter;
    }

    private Employee createAllEmployeesOption() {
        Employee allEmployeesOption = new Employee();
        allEmployeesOption.setFirstName("All");
        return allEmployeesOption;
    }

    private String getEmployeeLabel(final Employee employee) {
        return employee.getFirstName().equals("All") ? "All" : employee.getFirstName() + " " + employee.getLastName();
    }

    private void navigateToEditDocumentView(final Document document) {
        navigateToDocumentView(document, "edit");
    }

    private void navigateToDocumentView(final Document document) {
        navigateToDocumentView(document, "view");
    }

    private void navigateToDocumentView(final Document document, final String action) {
        getUI().ifPresent(ui -> ui.navigate(DocumentView.class, document.getId().toString() + "/" + action));
    }

    private void navigateToAddDocumentView() {
        getUI().ifPresent(ui -> ui.navigate(DocumentView.class, "new"));
    }

    private void configurePagination() {
        documentGrid.setPaginationBarMode(PagingGrid.PaginationBarMode.BOTTOM);
        documentGrid.setPageSize(PAGE_SIZE);
    }

    private void updateDocumentGrid(final DocumentType documentType, final Employee employee) {
        DocumentType finalDocumentType = isValidDocumentType(documentType) ? documentType : null;
        Employee finalEmployee = isValidEmployee(employee) ? employee : null;
        documentGrid.setPagingDataProvider((page, pageSize) ->
                (finalDocumentType == null && finalEmployee == null)
                        ? fetchDocuments((int) page, pageSize)
                        : fetchFilteredDocuments((int) page, pageSize, finalDocumentType, finalEmployee)
        );
        documentGrid.getDataProvider().refreshAll();
    }

    private boolean isValidDocumentType(final DocumentType documentType) {
        return documentType != null && !"All".equals(documentType.name());
    }

    private boolean isValidEmployee(final Employee employee) {
        return employee != null && !"All".equals(employee.getFirstName());
    }

    private List<Document> fetchFilteredDocuments(final int page,
                                                  final int pageSize,
                                                  final DocumentType documentType,
                                                  final Employee employee) {
        return documentService.findDocumentBy(documentType, employee, page, pageSize);
    }

    private List<Document> fetchDocuments(final int page, final int size) {
        int startIndex = page * size;
        return isSortOrderPresent()
                ? fetchSortedDocuments(startIndex, size)
                : documentService.findDocuments(startIndex, size);
    }

    private boolean isSortOrderPresent() {
        return !documentGrid.getSortOrder().isEmpty();
    }

    private List<Document> fetchSortedDocuments(final int start, final int pageSize) {
        GridSortOrder<Document> sortOrder = documentGrid.getSortOrder().getFirst();
        return documentService.findDocuments(start, pageSize,
                sortOrder.getSorted().getKey(),
                sortOrder.getDirection() == SortDirection.ASCENDING);
    }

    private void downloadDocument(final Document document) {
        StreamResource resource = createDocumentStreamResource(document);
        getUI().ifPresent(ui -> openDocumentStream(resource, ui));
    }

    private StreamResource createDocumentStreamResource(final Document document) {
        StreamResource resource = new StreamResource(document.getFileName(),
                () -> new ByteArrayInputStream(document.getFileData()));
        resource.setContentType("application/pdf");
        resource.setHeader("Content-Disposition", "attachment; filename=\"" + document.getFileName() + ".pdf\"");
        return resource;
    }

    private void openDocumentStream(final StreamResource resource, final UI ui) {
        StreamRegistration registration = ui.getSession().getResourceRegistry().registerResource(resource);
        ui.getPage().open(registration.getResourceUri().toString());
    }
}