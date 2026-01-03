package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.ContractType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true, length = 100)
    private String employeeId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 150)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_role_id")
    private JobRole jobRole;

    @Column(name = "project_preferences", columnDefinition = "TEXT")
    private String projectPreferences;

    @Column(columnDefinition = "TEXT")
    private String interests;

    // relations (optional but recommended to init)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeSkill> skills = new HashSet<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeCertification> certifications = new HashSet<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeLanguage> languages = new HashSet<>();

    @OneToMany(mappedBy = "employee")
    private Set<Assignment> assignments = new HashSet<>();

    @OneToMany(mappedBy = "createdBy")
    private Set<Assignment> createdAssignments = new HashSet<>();

    @OneToMany(mappedBy = "createdBy")
    private Set<StaffingRequest> createdStaffingRequests = new HashSet<>();

    @OneToMany(mappedBy = "employee")
    private Set<EmployeeApplication> applications = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        Employee other = (Employee) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
