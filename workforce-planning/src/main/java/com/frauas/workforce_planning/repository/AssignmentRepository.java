package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // 🔹 Find all assignments for a specific employee
    Set<Assignment> findByEmployee_Id(Long employeeId);

    // 🔹 Find assignments linked to a specific Staffing Request
    // Note: This uses the 'requestId' field name from your updated StaffingRequest entity
    List<Assignment> findByStaffingRequest_RequestId(Long requestId);

    // 🔹 Find assignments for a specific project
    List<Assignment> findByProject_Id(Long projectId);

    // 🔹 Find assignments created by a specific staffer/manager
    List<Assignment> findByCreatedBy_Id(Long employeeId);

    // 🔹 Filter assignments by status (e.g., 'ACTIVE', 'COMPLETED')
    List<Assignment> findByStatus(String status);
}