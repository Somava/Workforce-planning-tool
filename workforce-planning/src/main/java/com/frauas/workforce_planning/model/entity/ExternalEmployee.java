package com.frauas.workforce_planning.model.entity;

import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "external_employees"
)
public class ExternalEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_employee_id", nullable = false, columnDefinition = "text")
    private String externalEmployeeId;

    @Column(name = "provider", nullable = false, length = 150)
    private String provider;

    @Column(name = "contract_id", nullable = false, columnDefinition = "text")
    private String contractId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills", columnDefinition = "jsonb")
    private List<String> skills;

    @Column(name = "evaluation_score")
    private Double evaluationScore;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "wage_per_hour")
    private Double wagePerHour;

    @Column(name = "staffing_request_id")
    private Long staffingRequestId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "status")
    private String status;

    @Column(name = "received_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime receivedAt;
}
