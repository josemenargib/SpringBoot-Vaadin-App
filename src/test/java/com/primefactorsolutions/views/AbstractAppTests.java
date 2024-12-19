package com.primefactorsolutions.views;

import com.github.mvysny.fakeservlet.FakeRequest;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.SpringServlet;

import kotlin.jvm.functions.Function0;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.mvysny.kaributesting.v10.Routes;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles(value = "test")
public class AbstractAppTests {
    private static final Routes routes = new Routes().autoDiscoverViews("com.primefactorsolutions");

    @Autowired
    protected ApplicationContext ctx;

    protected void login(String user, String pass, final List<String> roles) {
        final List<SimpleGrantedAuthority> authorities =
                roles.stream().map(it -> new SimpleGrantedAuthority("ROLE_" + it)).collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authReq
                = new UsernamePasswordAuthenticationToken(new User(user, pass, authorities), pass, authorities);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(authReq);

        // however, you also need to make sure that ViewAccessChecker works properly;
        // that requires a correct MockRequest.userPrincipal and MockRequest.isUserInRole()
        final FakeRequest request = (FakeRequest) VaadinServletRequest.getCurrent().getRequest();
        request.setUserPrincipalInt(authReq);
        request.setUserInRole((principal, role) -> roles.contains(role));
    }

    protected void logout() {
        if (VaadinServletRequest.getCurrent() != null) {
            final FakeRequest request = (FakeRequest) VaadinServletRequest.getCurrent().getRequest();
            request.setUserPrincipalInt(null);
            request.setUserInRole((principal, role) -> false);
        }
    }

    @BeforeEach
    public void setup() {
        final Function0<UI> uiFactory = UI::new;
        final SpringServlet servlet = new MockSpringServlet(routes, ctx, uiFactory);
        MockVaadin.setup(uiFactory, servlet);
    }

    @AfterEach
    public void tearDown() {
        logout();
        MockVaadin.tearDown();
    }
}
