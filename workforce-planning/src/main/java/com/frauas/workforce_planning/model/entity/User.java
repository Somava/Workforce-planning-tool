package com.frauas.workforce_planning.model.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @JsonIgnore 
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Internal employee account
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    @JsonIgnoreProperties({"department", "supervisor", "createdStaffingRequests", "assignments"})
    private Employee employee;

    /**
     * External employee (optional)
     */
    @Column(name = "external_employee_id")
    private Long externalEmployeeId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Staffing requests assigned to this user for approval or processing.
     * ADDED: This fixes the 'No property assignedUser found' repository error.
     */
    @OneToMany(mappedBy = "assignedUser", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("assignedUser")
    @JsonIgnore
    private Set<StaffingRequest> staffingRequests = new HashSet<>();

    public boolean isExternal() {
        return externalEmployeeId != null;
    }   

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User other = (User) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}