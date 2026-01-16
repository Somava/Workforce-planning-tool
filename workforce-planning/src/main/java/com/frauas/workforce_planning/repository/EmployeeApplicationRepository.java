package com.frauas.workforce_planning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.EmployeeApplication;
import com.frauas.workforce_planning.model.enums.ApplicationStatus;

@Repository
public interface EmployeeApplicationRepository extends JpaRepository<EmployeeApplication, Long> {
         

         // It returns only the IDs of projects where the application is NOT withdrawn
    @Query("SELECT a.staffingRequest.requestId FROM EmployeeApplication a " +
           "WHERE a.employee.email = :email " +
           "AND a.status != com.frauas.workforce_planning.model.enums.ApplicationStatus.WITHDRAWN")
    List<Long> findRequestIdsByEmployeeEmail(@Param("email") String email);
    // 🔹 Find all applications made by a specific employee
    //Set<EmployeeApplication> findByEmployee_Id(Long employeeId);

    // 🔹 FIXED: Find applications for a specific staffing request
    // Uses 'requestId' to match the updated StaffingRequest entity
    List<EmployeeApplication> findByStaffingRequest_RequestId(Long requestId);

    // 🔹 Find applications by status (e.g., 'APPLIED', 'REJECTED', 'ACCEPTED')
    List<EmployeeApplication> findByStatus(ApplicationStatus status);

    // 🔹 Find applications handled/decided by a specific manager
    List<EmployeeApplication> findByDecisionBy_Id(Long employeeId);

    // 🔹 Find all applications for a specific employee by their database ID
    List<EmployeeApplication> findByEmployee_Id(Long employeeId);

}