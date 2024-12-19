package com.primefactorsolutions.model;

import com.google.common.collect.Lists;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Employee extends BaseEntity implements UserDetails {

    private String username;
    @NotNull(message = "El nombre no puede estar vacío")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre solo debe contener letras")
    private String firstName;
    @NotNull(message = "El apellido no puede estar vacío")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El apellido solo debe contener letras")
    private String lastName;
    private LocalDate birthday;
    @Pattern(regexp = "^[a-zA-Z ,]+$", message = "La ciudad de nacimiento solo debe contener letras, espacios o comas")
    private String birthCity;
    private String age;
    @Size(max = 50, message = "La dirección de residencia no debe exceder 50 caracteres")
    private String residenceAddress;
    @Size(max = 30, message = "La dirección local no debe exceder 100 caracteres")
    @Pattern(regexp = "^[a-zA-Z -]+$", message = "La dirección local solo debe contener letras y guion")
    private String localAddress;
    @Pattern(regexp = "^[0-9]+$", message = "El número de teléfono debe contener solo números")
    private String phoneNumber;
    @Email(message = "El correo personal no tiene un formato válido")
    private String personalEmail;
    @Pattern(regexp = "^[0-9]+$", message = "El número de teléfono debe contener solo números")
    private String phoneNumberProfesional;
    @Email(message = "El correo profesional no tiene un formato válido")
    private String profesionalEmail;

    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El cargo solo debe contener letras")
    private String position;
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre y apellido de contacto"
            + " de emergencia solo debe contener letras")
    private String emergencyCName;
    private String emergencyCAddress;
    @Pattern(regexp = "^[0-9]+$", message = "El teléfono de contacto de emergencia "
            + " debe contener solo números")
    private String emergencyCPhone;
    @Email(message = "El correo de contacto de emergencia no tiene un formato válido")
    private String emergencyCEmail;
    private String numberOfChildren;
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "El CI debe contener solo letras y números")
    private String ci;
    private String issuedIn;
    private String pTitle1;
    private String pTitle2;
    private String pTitle3;

    private String pStudy1;
    private String pStudy2;
    private String pStudy3;

    private String certification1;
    private String certification2;
    private String certification3;
    private String certification4;
    private String recognition;
    private String achievements;

    private String language1;
    private String language1Level;
    private String language2;
    private String language2Level;
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El código debe contener solo letras y números")
    private String cod;
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El lead manager solo debe contener letras")
    private String leadManager;

    private LocalDate dateOfEntry;
    private LocalDate dateOfExit;

    private String seniority;
    @Pattern(regexp = "^[0-9]+(\\.[0-9]{1,2})?$", message = "El salario debe ser un número con hasta dos decimales")
    private String salarytotal;
    private String salaryBasic;
    private String bonoProfesional;
    private String antiguedad;
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre del banco solo debe contener letras")
    private String bankName;
    @Pattern(regexp = "^[0-9]+$", message = "El número de cuenta debe contener solo números")
    private String accountNumber;

    private String gpss;
    private String sss;
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Los derechohabientes solo deben contener letras")
    private String beneficiarie1;
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Los derechohabientes solo deben contener letras")
    private String beneficiarie2;
    @Column(columnDefinition = "TEXT")
    private String profileImage;
    @Enumerated(EnumType.STRING)
    private Status status;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Lists.newArrayList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    public enum MaritalStatus {
        SINGLE,
        MARRIED,
        WIDOWED,
        DIVORCED
    }

    @Enumerated(EnumType.STRING)
    private Gender gender;

    public enum Gender {
        MALE,
        FEMALE
    }

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    public enum ContractType {
        CONTRATO_LABORAL,
        CONTRATO_CIVIL_O_SERVICIOS,
        CONTRATO_PLAZO_FIJO,
        CONSULTORIA_INTERNA,
        CONSULTORIA_EXTERNA,
        MIXTO,
        OTROS
    }

    @Size(max = 255, message = "El detalle del contrato no debe exceder 255 caracteres")
    private String otherContractDetail;
}