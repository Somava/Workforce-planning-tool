package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "staffing_request_skills",
    uniqueConstraints = @UniqueConstraint(columnNames = {"staffing_request_id", "skill_id"})
)
@Data
public class StaffingRequestSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staffing_request_id")
    private StaffingRequest staffingRequest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "required_level", length = 100)
    private String requiredLevel;
}
