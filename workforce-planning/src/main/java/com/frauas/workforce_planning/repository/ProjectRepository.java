package com.frauas.workforce_planning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.Project;
import com.frauas.workforce_planning.model.enums.ProjectStatus;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Corrected to use ProjectStatus Enum instead of String
    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByPublishedTrue();

    List<Project> findByNameContainingIgnoreCase(String name);

    // This traversal works perfectly now because of the @OneToMany in your Project entity
    List<Project> findByStaffingRequests_Department_Id(Long departmentId);
}