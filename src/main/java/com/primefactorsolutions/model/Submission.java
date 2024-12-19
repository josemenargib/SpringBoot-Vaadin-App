package com.primefactorsolutions.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Submission extends BaseEntity {
    @ManyToOne
    private Question question;

    @Lob
    private String response;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> results;

    private SubmissionStatus submissionStatus;

    @ManyToOne
    private Assessment assessment;
}
