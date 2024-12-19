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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("password-recovery")
@PageTitle("PFS Intra")
@AnonymousAllowed
public class PasswordRecoveryView extends VerticalLayout implements BeforeEnterObserver {

    public PasswordRecoveryView(final AccountService accountService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        final VerticalLayout vl = new VerticalLayout();
        vl.setJustifyContentMode(JustifyContentMode.CENTER);
        vl.setWidth("400px");

        final EmailField personalEmail = new EmailField("Personal Email");
        personalEmail.setRequired(true);

        final EmailField confirmPersonalEmail = new EmailField("Confirm Personal Email");
        confirmPersonalEmail.setRequired(true);

        final FormLayout formLayout = new FormLayout(personalEmail, confirmPersonalEmail);
        formLayout.setColspan(personalEmail, 3);
        formLayout.setColspan(confirmPersonalEmail, 3);

        final Button primaryButton = new Button("Submit");
        primaryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        primaryButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            if (personalEmail.getValue().equals(confirmPersonalEmail.getValue())) {
                accountService.sendResetPasswordEmail(personalEmail.getValue());
                getUI().ifPresent(ui -> ui.navigate(MainView.class));
            }
        });

        final Button secondaryButton = new Button("Cancel");
        final HorizontalLayout hl = new HorizontalLayout(secondaryButton, primaryButton);
        secondaryButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent ->
                getUI().ifPresent(ui -> ui.navigate(MainView.class)));

        vl.add(new H3("PFS - Password Recovery"));
        vl.add(formLayout);
        vl.add(hl);

        add(vl);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
    }
}