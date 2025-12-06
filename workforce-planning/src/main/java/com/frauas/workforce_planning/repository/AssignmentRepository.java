package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Set<Assignment> findByEmployee_Id(Long employeeId);
}
