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

    @Column(nullable = false, unique = true)
    private String name;      // e.g. "Java", "Python", "German B2"

    private String category;  // "Technical", "Language", ...

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "skill")
    private Set<EmployeeSkill> employeeSkills;
}
