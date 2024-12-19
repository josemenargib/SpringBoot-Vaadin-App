package com.primefactorsolutions.views;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

@Getter
public class BaseView extends Main {

    private final VerticalLayout currentPageLayout;

    public BaseView() {
        currentPageLayout = new VerticalLayout();
        add(currentPageLayout);
    }
}
