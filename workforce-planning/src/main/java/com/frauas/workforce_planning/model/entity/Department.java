package com.frauas.workforce_planning.model.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Link to the Project. 
     * Each project can have multiple departments.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties("departments")
    private Project project;

    // --- ID Columns for Manual Updates/Payloads ---

    @Column(name = "department_head_user_id")
    private Long departmentHeadUserId;

    @Column(name = "resource_planner_user_id")
    private Long resourcePlannerUserId;

    // --- Relationship Mappings ---

    /**
     * UPDATED: Changed to @ManyToOne.
     * This allows one User to be the head of multiple departments/projects.
     * Use insertable/updatable = false because we manage the ID via departmentHeadUserId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_head_user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"employee", "externalEmployee", "roles", "passwordHash", "password"})
    private User departmentHead;

    /**
     * UPDATED: Changed to @ManyToOne.
     * Allows one Resource Planner to be assigned to multiple departments.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_planner_user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"employee", "externalEmployee", "roles", "passwordHash", "password"})
    private User resourcePlanner;

    // --- Collections ---

    @OneToMany(mappedBy = "department")
    @JsonIgnore 
    private List<StaffingRequest> staffingRequests;

    @OneToMany(mappedBy = "department")
    @JsonIgnore 
    private List<Employee> employees;

    // --- Standard Methods ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department other = (Department) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}