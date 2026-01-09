package com.frauas.workforce_planning.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    // CHANGED: Removed unique = true because IT can exist in multiple projects
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * NEW: Link to the Project. 
     * Each of your 4 projects will have 3 departments.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties("departments")
    private Project project;

    // Inside DepartmentEntity.java

    @Column(name = "department_head_user_id")
    private Long departmentHeadUserId;

    // The Controller is looking for this EXACT name
    public Long getDepartmentHeadUserId() {
        return departmentHeadUserId;
    }

    public void setDepartmentHeadUserId(Long departmentHeadUserId) {
        this.departmentHeadUserId = departmentHeadUserId;
    }

    /**
     * UPDATED: Changed to @OneToOne because your requirement is for 12 UNIQUE heads.
     * The database has a UNIQUE constraint on this column now.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_head_user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"employee", "externalEmployee", "roles", "passwordHash", "password"})
    private User departmentHead;

    /**
     * List of Staffing Requests in this department.
     */
    @OneToMany(mappedBy = "department")
    @JsonIgnore 
    private List<StaffingRequest> staffingRequests;

    /**
     * List of Employees in this department.
     */
    @OneToMany(mappedBy = "department")
    @JsonIgnore 
    private List<Employee> employees;

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