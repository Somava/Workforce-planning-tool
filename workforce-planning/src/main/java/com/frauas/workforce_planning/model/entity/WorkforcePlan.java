package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workforce_plans")
@Data
@NoArgsConstructor
public class WorkforcePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the project this plan belongs to
    @OneToOne(optional = false)
    @JoinColumn(name = "project_id", unique = true)
    private Project project;

    @Column(name = "plan_name", length = 200)
    private String planName;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    // Total headcount or hours target for the project
    @Column(name = "target_headcount")
    private Integer targetHeadcount;

    @Column(name = "total_budget_allocated")
    private java.math.BigDecimal totalBudgetAllocated;

    @Column(name = "is_finalized")
    private Boolean isFinalized = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Audit field for who created the plan
    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
}