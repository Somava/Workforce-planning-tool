package com.frauas.workforce_planning.model.entity;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    // Keep these ignores to prevent the User -> Employee -> Department -> Project loop
    @JsonIgnoreProperties({"department", "supervisor", "createdStaffingRequests", "assignments", "user"})
    private Employee employee;

    @Column(name = "external_employee_id")
    private Long externalEmployeeId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    // This allows the User to show their Roles, but Role.java will NOT show Users
    @JsonIgnoreProperties("users") 
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "assignedUser", fetch = FetchType.LAZY)
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
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}