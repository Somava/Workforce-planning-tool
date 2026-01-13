package com.frauas.workforce_planning.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore // <--- ADD THIS to break the infinite recursion loop
    private Set<User> users;
}