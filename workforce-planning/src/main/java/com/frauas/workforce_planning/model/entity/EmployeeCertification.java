package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(
    name = "employee_certifications",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "certification_id"})
)
@Data
public class EmployeeCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "certification_id")
    private Certification certification;

    @Column(length = 255)
    private String issuer;

    @Column(name = "date_obtained")
    private LocalDate dateObtained;

    @Column(name = "valid_until")
    private LocalDate validUntil;
}
