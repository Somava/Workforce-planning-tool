package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "departments")
@Data
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @ManyToOne
    @JoinColumn(name = "department_head_user_id")
    private User departmentHeadUser;
}
