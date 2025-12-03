package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "employee_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "skill_id"})
)
@Data
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    private SkillLevel skillLevel;

    private Integer yearsOfExperience;

    private String certification;
}
