package com.primefactorsolutions.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.EmailField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.mvysny.kaributesting.v10.LocatorJ.*;

public class CandidateViewTests extends AbstractAppTests {
    @BeforeEach
    public void login() {
        login("user", "user", List.of("user"));
    }

    @Test
    public void sayHelloWithMockBean() {
        UI.getCurrent().navigate(CandidateView.class, "new");
        _setValue(_get(EmailField.class, spec -> spec.withLabel("Email")), "foo@test.com");
        _click(_get(Button.class, spec -> spec.withText("Save")));
    }
}
