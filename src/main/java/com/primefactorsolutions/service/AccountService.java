package com.primefactorsolutions.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.primefactorsolutions.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class AccountService {
    private final EmailService emailService;
    private final EmployeeService employeeService;
    private final String secret;

    public AccountService(final EmailService emailService, final EmployeeService employeeService,
                           @Value("${application.jwtSecret}") final String secret) {
        this.emailService = emailService;
        this.employeeService = employeeService;
        this.secret = secret;
    }

    public void sendResetPasswordEmail(final String personalEmail) {
        final Employee employee = employeeService.getEmployeeByPersonalEmail(personalEmail);

        if (employee == null) {
            log.warn("Could not find employee for email {}", personalEmail);
            return;
        }

        final String link = createResetPasswordLink(employee.getUsername());
        final String content = "Visit this link to reset your password: " + link;
        emailService.sendEmail(personalEmail, "PFS - Reset Password", content);
    }

    public void resetPassword(final String username, final String newPassword, final String token) {
        DecodedJWT decodedJWT;

        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("pfs")
                    .build();

            decodedJWT = verifier.verify(token);
            final Instant expiry = decodedJWT.getExpiresAtAsInstant();
            final String claim = decodedJWT.getClaim("username").asString();

            if (expiry.isBefore(Instant.now())
                    || !username.equals(claim)) {
                log.warn("token invalid {} {} {}", username, claim, expiry);
                return;
            }
        } catch (JWTVerificationException e) {
            log.warn("error updating password", e);
            return;
        }

        final Employee employee = employeeService.getDetachedEmployeeByUsername(username);

        if (employee == null) {
            log.warn("Could not find employee for username {}", username);
            return;
        }

        if (StringUtils.isBlank(newPassword) || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password should be at least 8 chars long");
        }

        employeeService.updatePassword(employee, newPassword);

        log.info("updated password for {}", username);
    }

    private String createResetPasswordLink(final String username) {
        String token = "";

        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            token = JWT.create()
                    .withIssuer("pfs")
                    .withClaim("username", username)
                    .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RuntimeException(e);
        }

        return String.format("https://intra.primefactorsolutions.com/reset-password?username=%s&token=%s", username,
                token);
    }
}
