package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Assessment;
import com.primefactorsolutions.service.AssessmentService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.vaadin.addon.stefan.clipboard.ClientsideClipboard;
import org.vaadin.firitin.components.grid.VGrid;

import java.util.stream.Stream;

@SpringComponent
@Scope("prototype")
@PageTitle("Assessments")
@Route(value = "/assessments", layout = MainLayout.class)
@PermitAll
public class AssessmentsListView extends BaseView {

    public AssessmentsListView(final AssessmentService assessmentService) {
        final HorizontalLayout hl = new HorizontalLayout();
        final Button addAssessment = new Button("Add Assessment");
        addAssessment.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            this.getUI().flatMap(ui -> ui.navigate(AssessmentView.class, "new"));
        });
        hl.add(addAssessment);

        final VGrid<Assessment> grid = new VGrid<>(Assessment.class);
        grid.setColumns("id", "candidate.email");
        final Grid.Column<Assessment> statusColumn = grid.addColumn((ValueProvider<Assessment, Object>) assessment ->
                assessment.getAssessmentEvents().isEmpty()
                        ? "N/A"
                        : assessment.getAssessmentEvents().getLast().getStatus().name());
        statusColumn.setHeader("Status");

        grid.addComponentColumn((ValueProvider<Assessment, Component>) assessment -> {
            var result = new Button("Result", event ->
                    this.getUI().flatMap(ui -> ui.navigate(SubmissionView.class, assessment.getId().toString()))
            );
            result.setEnabled(assessment.isCompleted());

            return result;
        });

        grid.addComponentColumn((ValueProvider<Assessment, Component>) assessment -> new Button("Copy Link", event ->
                ClientsideClipboard.writeToClipboard(
                        String.format("email: %s link: https://careers.primefactorsolutions.com/evaluation/%s",
                                assessment.getCandidate().getEmail(),
                                assessment.getId()))
        ));
        grid.addComponentColumn((ValueProvider<Assessment, Component>) assessment ->
                new Button("Send Email", event -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Send Link Email");
            dialog.setText(String.format("Enviar link por email al candidato %s?",
                    assessment.getCandidate().getEmail()));
            dialog.setCancelable(true);
            dialog.setConfirmText("Enviar");
            dialog.setConfirmButtonTheme("primary");
            dialog.addConfirmListener((ComponentEventListener<ConfirmDialog.ConfirmEvent>) confirmEvent -> {
                try {
                    assessmentService.sendEmail(assessment);
                } catch (Exception e) {
                    Notification.show("Error sending email: " + e.getMessage(), 10_000,
                            Notification.Position.TOP_CENTER);
                }
            });
            dialog.open();
        }));

        grid.setDataProvider(new DataProvider<>() {
            @Override
            public boolean isInMemory() {
                return false;
            }

            @Override
            public int size(final Query<Assessment, Object> query) {
                return assessmentService.getAssessments().size();
            }

            @SuppressWarnings("unused")
            @Override
            public Stream<Assessment> fetch(final Query<Assessment, Object> query) {
                int limit = query.getLimit();
                int pagerSize = query.getPageSize();
                int page = query.getPage();

                return assessmentService.getAssessments().stream();
            }

            @Override
            public void refreshItem(final Assessment assessment) {
                // no-op
            }

            @Override
            public void refreshAll() {

            }

            @Override
            public Registration addDataProviderListener(final DataProviderListener<Assessment> dataProviderListener) {
                return null;
            }
        });
        grid.setAllRowsVisible(true);

        getCurrentPageLayout().add(hl, grid);
    }

}
