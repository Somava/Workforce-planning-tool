package com.frauas.workforce_planning.model.entity;
import com.frauas.workforce_planning.model.enums.MatchingAvailability;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.math.BigDecimal;


import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.frauas.workforce_planning.model.enums.ContractType;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"employees", "departmentHead", "staffingRequests"})
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_role_id")
    @JsonIgnoreProperties("employees")
    private Role defaultRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    @JsonIgnoreProperties({"subordinates", "department", "createdStaffingRequests", "assignments", "supervisor"})
    private Employee supervisor;

    @Column(name = "primary_location", length = 150)
    private String primaryLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 50)
    private ContractType contractType;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "wage_per_hour")
    private BigDecimal wagePerHour;

    @Column(name = "total_hours_per_week")
    private Integer totalHoursPerWeek;

    @Column(name = "remaining_hours_per_week")
    private Integer remainingHoursPerWeek;

    @Column(name = "performance_rating")
    private Double performanceRating;


    @Column(name = "emergency_contact", length = 255)
    private String emergencyContact;

    @Column(name = "availability_start")
    private LocalDate availabilityStart;

    @Column(name = "availability_end")
    private LocalDate availabilityEnd;
    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "matching_availability",  
        nullable = false,
        length = 50
    )
    private MatchingAvailability matchingAvailability = MatchingAvailability.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_role_id")
    @JsonIgnoreProperties({"staffingRequests", "employees"})
    private JobRole jobRole;

    @Column(name = "project_preferences", columnDefinition = "TEXT")
    private String projectPreferences;

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests;

    @Type(JsonType.class)
    @Column(name = "skills", columnDefinition = "jsonb")
    private List<String> skills;

    // --- Relations with Recursion Breaks ---

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("employee")
    private Set<EmployeeCertification> certifications = new HashSet<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("employee")
    private Set<EmployeeLanguage> languages = new HashSet<>();

    @OneToMany(mappedBy = "employee")
    @JsonIgnore // Stops Project -> Assignment -> Employee loop
    private Set<Assignment> assignments = new HashSet<>();

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore
    private Set<Assignment> createdAssignments = new HashSet<>();

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore // Stops Request -> Employee -> CreatedRequests loop
    private Set<StaffingRequest> createdStaffingRequests = new HashSet<>();

    @OneToMany(mappedBy = "employee")
    @JsonIgnoreProperties("employee")
    private Set<EmployeeApplication> applications = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee other = (Employee) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
    @OneToOne(mappedBy = "employee")
    @JsonIgnore
    private User user;
}