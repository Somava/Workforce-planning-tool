package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "skills")
@Data
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @OneToMany(mappedBy = "skill")
    private Set<EmployeeSkill> employeeSkills;

    @OneToMany(mappedBy = "skill")
    private Set<StaffingRequestSkill> staffingRequestSkills;
}
