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

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    /**
     * FK: departments.department_head_user_id -> users.id
     */
    @Column(name = "department_head_user_id")
    private Long departmentHeadUserId;

    /**
     * Read-only object mapping.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_head_user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"employee", "externalEmployee", "roles", "passwordHash", "password"})
    private User departmentHead;

    /**
     * List of Staffing Requests in this department.
     */
    @OneToMany(mappedBy = "department")
    @JsonIgnore // STOP recursion: StaffingRequest -> Department -> StaffingRequest (List)
    private List<StaffingRequest> staffingRequests;

    /**
     * List of Employees in this department.
     */
    @OneToMany(mappedBy = "department")
    @JsonIgnore // STOP recursion: StaffingRequest -> Department -> Employee (List)
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