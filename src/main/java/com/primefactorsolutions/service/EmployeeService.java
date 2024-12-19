package com.primefactorsolutions.service;
import com.google.common.base.Strings;
import com.primefactorsolutions.model.Employee;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanComparator;
import com.primefactorsolutions.repositories.EmployeeRepository;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.*;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Collections;

@Service
@AllArgsConstructor
public class EmployeeService {
    private static final String USERPASSWORD = "userPassword";
    private static final String OBJECTCLASS = "objectclass";
    private static final String ORGANIZATIONAL_PERSON = "organizationalPerson";
    private static final String INET_ORG_PERSON = "inetOrgPerson";
    private static final String TOP = "top";
    private static final String PERSON = "person";
    public static final String BASE_DN = "dc=primefactorsolutions,dc=com";

    private final EmployeeRepository employeeRepository;
    private final LdapTemplate ldapTemplate;
    private final EntityManager entityManager;

    protected Name buildDn(final Employee employee) {
        return LdapNameBuilder.newInstance(BASE_DN)
                .add("ou", "users")
                .add("uid", employee.getUsername())
                .build();
    }

    public Employee getDetachedEmployeeByUsername(final String username) {
        final Employee employee = employeeRepository.findByUsername(username).orElse(null);

        if (employee != null) {
            entityManager.detach(employee);
            return employee;
        }

        return null;
    }

    public String getTeamLeadName(final UUID teamId) {
        // Obtiene una lista de empleados con el rol de lead_manager en el equipo especificado
        List<Employee> leadManagers = employeeRepository.findByTeamIdAndLeadManagerTrue(teamId);

        // Retorna el nombre del primer empleado encontrado, o "No asignado" si la lista está vacía
        return leadManagers.isEmpty()
                ? "No asignado"
                : leadManagers.get(0).getFirstName() + " " + leadManagers.get(0).getLastName();
    }


    public List<Employee> findEmployees(
            final int start, final int pageSize, final String sortProperty, final boolean asc) {
        List<Employee> employees = employeeRepository.findAll();

        int end = Math.min(start + pageSize, employees.size());
        employees.sort(new BeanComparator<>(sortProperty));

        if (!asc) {
            Collections.reverse(employees);
        }

        return employees.subList(start, end);
    }

    public List<Employee> findEmployees(final int start, final int pageSize) {
        List<Employee> employees = employeeRepository.findAll();

        int end = Math.min(start + pageSize, employees.size());
        return employees.subList(start, end);
    }

    public Employee getEmployeeByPersonalEmail(final String email) {
        return employeeRepository.findByPersonalEmail(email).orElse(null);
    }

    public Employee createOrUpdate(final Employee employee) {
        if (employee.getId() == null) {
            final Name dn = buildDn(employee);
            // ldapClient.bind(dn).attributes(buildAttributes(employee)).execute();
            ldapTemplate.bind(dn, null, buildAttributes(employee));
        }

        return employeeRepository.save(employee);
    }

    public Employee getEmployee(final UUID id) {
        final Optional<Employee> employee = employeeRepository.findById(id);

        return employee.orElse(null);
    }

    private Attributes buildAttributes(final Employee employee) {
        final Attributes attrs = new BasicAttributes();
        final BasicAttribute ocattr = new BasicAttribute(OBJECTCLASS);
        ocattr.add(TOP);
        ocattr.add(PERSON);
        ocattr.add(ORGANIZATIONAL_PERSON);
        ocattr.add(INET_ORG_PERSON);
        attrs.put(ocattr);
        attrs.put("cn", String.format("%s %s", employee.getFirstName(), employee.getLastName()));
        attrs.put("sn", String.format("%s %s", employee.getFirstName(), employee.getLastName()));
        attrs.put("uid", employee.getUsername());
        attrs.put(USERPASSWORD, String.format("%s%s", employee.getUsername(), 123));

        return attrs;
    }

    public void updatePassword(final Employee employee, final String newPassword) {
        final Attribute attr = new BasicAttribute(USERPASSWORD, newPassword);
        final ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);

        ldapTemplate.modifyAttributes(buildDn(employee), new ModificationItem[] {item});
    }

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> findEmployeesByTeam(final String teamName) {
        return employeeRepository.findByTeamName(teamName);
    }
}