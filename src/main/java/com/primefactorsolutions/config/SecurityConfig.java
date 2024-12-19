package com.primefactorsolutions.config;

import com.primefactorsolutions.model.Employee;
import com.primefactorsolutions.service.EmployeeService;
import com.primefactorsolutions.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {
    @Value("${spring.ldap.url}")
    private String ldapUrl;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
                auth
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/**/*.jpg"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/**/*.png"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/**/*.css"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/**/*.scss"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/**/*.js")
                        ).permitAll())
                .headers(headers -> headers.frameOptions(frameOptionsConfig -> {
                    //no-op
                }).disable())
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")));
        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    @Bean
    public AuthenticationManager authenticationManager(final UserDetailsContextMapper userDetailsContextMapper) {
        final DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(
                String.format("%s/dc=primefactorsolutions,dc=com", ldapUrl));
        contextSource.setCacheEnvironmentProperties(false);
        final LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserDnPatterns("uid={0},ou=users");
        factory.setUserDetailsContextMapper(userDetailsContextMapper);

        return factory.createAuthenticationManager();
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper(final EmployeeService employeeService) {
        return new LdapUserDetailsMapper() {
            @Override
            public UserDetails mapUserFromContext(final DirContextOperations ctx, final String username,
                                                  final Collection<? extends GrantedAuthority> authorities) {
                final UserDetails details = super.mapUserFromContext(ctx, username, authorities);
                final Employee employee = employeeService.getDetachedEmployeeByUsername(details.getUsername());

                return employee == null ? details : employee;
            }
        };
    }
}
