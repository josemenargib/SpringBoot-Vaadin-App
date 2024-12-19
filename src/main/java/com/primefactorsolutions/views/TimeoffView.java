package com.primefactorsolutions.views;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;

@SpringComponent
@PermitAll
@Scope("prototype")
@PageTitle("Timeoff")
@Route(value = "/timeoffs/me", layout = MainLayout.class)
public class TimeoffView extends Main {
}
