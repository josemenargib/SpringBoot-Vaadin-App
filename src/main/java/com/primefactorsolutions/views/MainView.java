package com.primefactorsolutions.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Home")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class MainView extends Main {

    public MainView() {
        add(new Text("Welcome"));
    }
}
