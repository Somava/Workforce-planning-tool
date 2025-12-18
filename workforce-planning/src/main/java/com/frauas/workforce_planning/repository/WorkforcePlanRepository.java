package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.WorkforcePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkforcePlanRepository extends JpaRepository<WorkforcePlan, Long> {
}