package com.primefactorsolutions.model;

import jakarta.persistence.*;

import lombok.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Document extends BaseEntity {
    private String fileName;
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] fileData;
    private String creator;
}
