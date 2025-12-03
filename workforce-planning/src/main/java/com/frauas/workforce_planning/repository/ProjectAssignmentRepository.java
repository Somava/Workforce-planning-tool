package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
}
