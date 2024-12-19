package com.primefactorsolutions.views.util;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MenuBarUtils {

    public static MenuItem createIconItem(final MenuBar menu, final VaadinIcon iconName, final String ariaLabel) {
        final Icon icon = new Icon(iconName);
        final MenuItem item = menu.addItem(icon);
        item.setAriaLabel(ariaLabel);

        return item;
    }
}
