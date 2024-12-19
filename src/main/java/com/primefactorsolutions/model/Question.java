package com.primefactorsolutions.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Question extends BaseEntity {
    @Column(unique = true)
    private String title;
    @Lob
    private String description;
    @Lob
    private String content;
    private Integer timeMinutes;
}
