package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.JobRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
    Optional<JobRole> findByName(String name);
}
