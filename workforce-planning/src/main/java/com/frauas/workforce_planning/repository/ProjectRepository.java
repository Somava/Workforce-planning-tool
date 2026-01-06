package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 🔹 Find projects by status (e.g., 'PLANNED', 'ACTIVE', 'COMPLETED')
    List<Project> findByStatus(String status);

    // 🔹 Find all published projects for the public job board
    List<Project> findByPublishedTrue();

    // 🔹 Search projects by name (case-insensitive)
    List<Project> findByNameContainingIgnoreCase(String name);

    // 🔹 Find projects that have staffing requests for a specific department
    // This traverses Project -> StaffingRequests -> Department -> ID
    List<Project> findByStaffingRequests_Department_Id(Long departmentId);
}