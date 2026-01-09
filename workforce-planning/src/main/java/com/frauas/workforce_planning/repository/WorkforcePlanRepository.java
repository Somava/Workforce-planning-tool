package com.frauas.workforce_planning.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.WorkforcePlan;

@Repository
public interface WorkforcePlanRepository extends JpaRepository<WorkforcePlan, Long> {

    // 🔹 Find the plan associated with a specific project
    Optional<WorkforcePlan> findByProject_Id(Long projectId);

    @Query("SELECT w FROM WorkforcePlan w JOIN w.project p JOIN StaffingRequest s ON s.project = p WHERE s.department.id = :deptId")
    List<WorkforcePlan> findByDepartmentId(@Param("deptId") Long deptId);

    // 🔹 Find plans that have been recently updated
    List<WorkforcePlan> findAllByOrderByUpdatedAtDesc();


}