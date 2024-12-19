package com.primefactorsolutions.views;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;

@SpringComponent
@Scope("prototype")
@PageTitle("Timesheets")
@Route(value = "/timesheets", layout = MainLayout.class)
@PermitAll
public class TimesheestReportView extends Main {
}
