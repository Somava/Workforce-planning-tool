package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "employee_languages",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "language_id"})
)
@Data
public class EmployeeLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(name = "proficiency_level", length = 50)
    private String proficiencyLevel;
}
