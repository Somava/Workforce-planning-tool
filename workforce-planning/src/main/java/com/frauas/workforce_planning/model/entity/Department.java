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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties("departments")
    private Project project;

    @Column(name = "department_head_user_id")
    private Long departmentHeadUserId;

    @Column(name = "resource_planner_user_id")
    private Long resourcePlannerUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_head_user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"employee", "externalEmployee", "roles", "passwordHash", "password"})
    private User departmentHead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_planner_user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"employee", "externalEmployee", "roles", "passwordHash", "password"})
    private User resourcePlanner;

    @OneToMany(mappedBy = "department")
    @JsonIgnore 
    private List<StaffingRequest> staffingRequests;

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