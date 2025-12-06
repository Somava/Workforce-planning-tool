package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.EmployeeApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface EmployeeApplicationRepository extends JpaRepository<EmployeeApplication, Long> {
    Set<EmployeeApplication> findByEmployee_Id(Long employeeId);
}
