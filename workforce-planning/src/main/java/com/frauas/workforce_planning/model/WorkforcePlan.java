package com.frauas.workforce_planning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class WorkforcePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String status;
    private String department;
}