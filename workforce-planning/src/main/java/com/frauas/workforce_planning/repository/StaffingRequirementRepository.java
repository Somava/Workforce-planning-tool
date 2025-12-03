package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.StaffingRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffingRequirementRepository extends JpaRepository<StaffingRequirement, Long> {
}
