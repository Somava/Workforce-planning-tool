package com.frauas.workforce_planning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.EmployeeApplication;

@Repository
public interface EmployeeApplicationRepository extends JpaRepository<EmployeeApplication, Long> {

    // 🔹 Find all applications made by a specific employee
    //Set<EmployeeApplication> findByEmployee_Id(Long employeeId);

    // 🔹 FIXED: Find applications for a specific staffing request
    // Uses 'requestId' to match the updated StaffingRequest entity
    List<EmployeeApplication> findByStaffingRequest_RequestId(Long requestId);

    // 🔹 Find applications by status (e.g., 'APPLIED', 'REJECTED', 'ACCEPTED')
    List<EmployeeApplication> findByStatus(String status);

    // 🔹 Find applications handled/decided by a specific manager
    List<EmployeeApplication> findByDecisionBy_Id(Long employeeId);

    // 🔹 Find all applications for a specific employee by their database ID
    List<EmployeeApplication> findByEmployee_Id(Long employeeId);

}