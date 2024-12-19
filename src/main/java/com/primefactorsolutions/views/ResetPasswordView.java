package com.primefactorsolutions.views;

import com.primefactorsolutions.service.AccountService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

@Route("reset-password")
@PageTitle("PFS Intra")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {
    private String username;
    private String token;

    public ResetPasswordView(final AccountService accountService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        final VerticalLayout vl = new VerticalLayout();
        vl.setJustifyContentMode(JustifyContentMode.CENTER);
        vl.setWidth("400px");

        final PasswordField password = new PasswordField("Password");
        final PasswordField confirmPassword = new PasswordField("Confirm Password");

        final FormLayout formLayout = new FormLayout(password, confirmPassword);
        formLayout.setColspan(password, 3);
        formLayout.setColspan(confirmPassword, 3);

        final Button primaryButton = new Button("Submit");
        primaryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        primaryButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            accountService.resetPassword(username, password.getValue(), token);
            getUI().ifPresent(ui -> ui.navigate(MainView.class));
        });

        final Button secondaryButton = new Button("Cancel");
        secondaryButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent ->
                getUI().ifPresent(ui -> ui.navigate(MainView.class)));

        HorizontalLayout hl = new HorizontalLayout(secondaryButton, primaryButton);

        vl.add(new H3("PFS - Reset Password"));
        vl.add(formLayout);
        vl.add(hl);

        add(vl);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
        final Location location = beforeEnterEvent.getLocation();
        final QueryParameters queryParameters = location.getQueryParameters();

        this.username = queryParameters.getParameters().getOrDefault("username", List.of()).stream()
                .findFirst().orElse(null);
        this.token = queryParameters.getParameters().getOrDefault("token", List.of()).stream()
                .findFirst().orElse(null);
    }
}