package com.primefactorsolutions.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@SuppressWarnings("unused")
@Route("init-account")
@PageTitle("PFS Intra")
@AnonymousAllowed
public class InitAccountView extends VerticalLayout implements BeforeEnterObserver {

    public InitAccountView() {
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

        final Button secondaryButton = new Button("Cancel");
        HorizontalLayout hl = new HorizontalLayout(secondaryButton, primaryButton);

        vl.add(new H3("Set Account Password"));
        vl.add(formLayout);
        vl.add(hl);

        add(vl);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
    }
}