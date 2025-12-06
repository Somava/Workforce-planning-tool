package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    Set<EmployeeSkill> findByEmployee_Id(Long employeeId);
}
