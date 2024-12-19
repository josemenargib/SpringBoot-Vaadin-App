package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Employee;
import com.primefactorsolutions.service.EmployeeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.vaadin.firitin.components.grid.PagingGrid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;

import java.util.List;

@SpringComponent
@Scope("prototype")
@PageTitle("Employees")
@Route(value = "/employees", layout = MainLayout.class)
@PermitAll
public class EmployeesListView extends BaseView {

    private final EmployeeService employeeService;
    private final PagingGrid<Employee> table = new PagingGrid<>(Employee.class);

    public EmployeesListView(final EmployeeService employeeService) {
        this.employeeService = employeeService;
        setupView();
        refreshGrid();
    }

    private void setupView() {
        configureTable();
        getCurrentPageLayout().add(createAddEmployeeButton());
        getCurrentPageLayout().add(table);
    }

    private void configureTable() {
        table.setColumns("firstName", "lastName", "status");
        addEditButtonColumn("View", this::navigateToEmployeeView);
        addEditButtonColumn("Edit", this::navigateToEditView);
        setupPagingGrid();
    }

    private void addEditButtonColumn(final String label, final ButtonClickHandler handler) {
        table.addComponentColumn(employee -> createButton(label, () -> handler.handle(employee)));
    }

    private Button createButton(final String label, final Runnable onClickAction) {
        Button button = new Button(label);
        button.addClickListener(event -> onClickAction.run());
        return button;
    }

    private Button createAddEmployeeButton() {
        return createButton("Add Employee", this::navigateToAddEmployeeView);
    }

    private void navigateToEditView(final Employee employee) {
        getUI().ifPresent(ui -> ui.navigate(EmployeeView.class, employee.getId().toString() + "/edit"));
    }

    private void navigateToEmployeeView(final Employee employee) {
        getUI().ifPresent(ui -> ui.navigate(EmployeeView.class, employee.getId().toString() + "/view"));
    }

    private void navigateToAddEmployeeView() {
        getUI().ifPresent(ui -> ui.navigate(EmployeeView.class, "new"));
    }

    private void setupPagingGrid() {
        table.setPaginationBarMode(PagingGrid.PaginationBarMode.BOTTOM);
        table.setPageSize(Constants.PAGE_SIZE);
    }

    private void refreshGrid() {
        table.setPagingDataProvider((page, pageSize) -> fetchEmployees((int) page, pageSize));
    }

    private List<Employee> fetchEmployees(final int page, final int pageSize) {
        int start = page * pageSize;
        if (hasSortOrder()) {
            return fetchSortedEmployees(start, pageSize);
        }
        return employeeService.findEmployees(start, pageSize);
    }

    private boolean hasSortOrder() {
        return !table.getSortOrder().isEmpty();
    }

    private List<Employee> fetchSortedEmployees(final int start, final int pageSize) {
        GridSortOrder<Employee> sortOrder = table.getSortOrder().getFirst();
        return employeeService.findEmployees(start, pageSize,
                sortOrder.getSorted().getKey(),
                sortOrder.getDirection() == SortDirection.ASCENDING);
    }

    @FunctionalInterface
    private interface ButtonClickHandler {
        void handle(Employee employee);
    }
}