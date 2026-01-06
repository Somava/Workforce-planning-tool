package com.frauas.workforce_planning.repository;

import com.frauas.workforce_planning.model.entity.StaffingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffingRequestRepository extends JpaRepository<StaffingRequest, Long> {

    // 🔹 Primary key access (request_id)
    Optional<StaffingRequest> findByRequestId(Long requestId);

    // 🔹 FK-based queries (safe, derived)
    List<StaffingRequest> findByProject_Id(Long projectId);

    List<StaffingRequest> findByDepartment_Id(Long departmentId);

    List<StaffingRequest> findByAssignedUser_Id(Long userId);

    List<StaffingRequest> findByStatus(String status);

    // 🔹 JSONB required_skills search (PostgreSQL)
    @Query(
        value = """
            SELECT *
            FROM staffing_requests
            WHERE required_skills @> CAST(:skills AS jsonb)
        """,
        nativeQuery = true
    )
    List<StaffingRequest> findByRequiredSkills(@Param("skills") String skillsJson);
}