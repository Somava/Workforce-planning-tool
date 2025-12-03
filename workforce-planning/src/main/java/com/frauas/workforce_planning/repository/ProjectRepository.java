package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
