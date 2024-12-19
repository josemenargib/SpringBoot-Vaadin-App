package com.primefactorsolutions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Candidate extends BaseEntity {
    @Column(unique = true)
    private String email;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "candidate")
    private List<Assessment> assessments;
}
