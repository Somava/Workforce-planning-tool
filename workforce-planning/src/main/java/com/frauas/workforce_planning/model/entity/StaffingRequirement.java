package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "staffing_requirements")
@Data
public class StaffingRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "required_role", nullable = false)
    private String requiredRole;

    @Column(name = "number_of_employees", nullable = false)
    private Integer numberOfEmployees;

    @Column(name = "capacity_hours_per_week", nullable = false)
    private Integer capacityHoursPerWeek;

    private Integer priority;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Join table staffing_requirement_skills
    @ManyToMany
    @JoinTable(
            name = "staffing_requirement_skills",
            joinColumns = @JoinColumn(name = "staffing_requirement_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> requiredSkills;
}
