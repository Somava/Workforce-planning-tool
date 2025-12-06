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

    // company HR ID
    @Column(name = "employee_id", unique = true, length = 100)
    private String employeeId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 150)
    private String department;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    @Column(name = "primary_location", length = 150)
    private String primaryLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 50)
    private ContractType contractType;

    @Column(name = "working_time_model", length = 100)
    private String workingTimeModel;

    @Column(name = "emergency_contact", length = 255)
    private String emergencyContact;

    @Column(name = "availability_start")
    private LocalDate availabilityStart;

    @Column(name = "availability_end")
    private LocalDate availabilityEnd;

    @ManyToOne
    @JoinColumn(name = "job_role_id")
    private JobRole jobRole;

    @Column(name = "project_preferences", columnDefinition = "TEXT")
    private String projectPreferences;

    @Column(columnDefinition = "TEXT")
    private String interests;

    // relations

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeSkill> skills;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeCertification> certifications;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeLanguage> languages;

    @OneToMany(mappedBy = "employee")
    private Set<Assignment> assignments;

    @OneToMany(mappedBy = "createdBy")
    private Set<Assignment> createdAssignments;

    @OneToMany(mappedBy = "createdBy")
    private Set<StaffingRequest> createdStaffingRequests;

    @OneToMany(mappedBy = "employee")
    private Set<EmployeeApplication> applications;
}
