package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.ContractType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "employees")
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String department;

    @Column(name = "org_unit")
    private String orgUnit;

    @Column(name = "primary_location")
    private String primaryLocation;

    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type")
    private ContractType contractType;

    private Integer workingHoursPerWeek;

    private String emergencyContact;

    @Column(columnDefinition = "TEXT")
    private String projectPreferences;

    @Column(columnDefinition = "TEXT")
    private String interests;

    private LocalDate availabilityStart;
    private LocalDate availabilityEnd;

    @Column(columnDefinition = "TEXT")
    private String plannedAbsencesNote;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeSkill> skills;
}
