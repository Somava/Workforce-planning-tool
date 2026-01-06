package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.WorkforcePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkforcePlanRepository extends JpaRepository<WorkforcePlan, Long> {

    // 🔹 Find the plan associated with a specific project
    Optional<WorkforcePlan> findByProject_Id(Long projectId);

    // 🔹 Find all plans for a specific department
    // (Navigates: WorkforcePlan -> Project -> StaffingRequests -> Department)
    List<WorkforcePlan> findByProject_StaffingRequests_Department_Id(Long departmentId);

    // 🔹 Find plans that have been recently updated
    List<WorkforcePlan> findAllByOrderByUpdatedAtDesc();
}