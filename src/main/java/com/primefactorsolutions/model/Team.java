package com.primefactorsolutions.model;

import jakarta.persistence.*;

import lombok.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Team extends BaseEntity {
    private String name;
}
