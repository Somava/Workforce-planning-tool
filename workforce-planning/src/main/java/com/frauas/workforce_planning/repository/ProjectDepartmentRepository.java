package com.frauas.workforce_planning.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.ProjectDepartment;

@Repository
public interface ProjectDepartmentRepository extends JpaRepository<ProjectDepartment, Long> {

    ProjectDepartment findByProject_IdAndDepartment_Id(Long projectId, Long departmentId);

    Optional<ProjectDepartment> findByProject_IdAndDepartmentHeadUser_Id(Long projectId, Long deptHeadUserId);

    Optional<ProjectDepartment> findByProject_IdAndResourcePlannerUser_Id(Long projectId, Long resourcePlannerUserId);
}
