package com.frauas.workforce_planning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 🔹 Find projects by their status
    List<Project> findByStatus(String status);
}