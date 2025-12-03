package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "project_assignments")
@Data
public class ProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "staffing_requirement_id")
    private StaffingRequirement staffingRequirement;

    private String roleInProject;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer effortHoursPerWeek;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    private Integer projectPriority;

    private Integer performanceRating;

    @Column(columnDefinition = "TEXT")
    private String feedback;
}
