package com.frauas.workforce_planning.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.RequestStatus;

@Repository
public interface StaffingRequestRepository extends JpaRepository<StaffingRequest, Long> {

    // Primary key access
    Optional<StaffingRequest> findByRequestId(Long requestId);
    // 🔹 The New Security Method for Applying
    Optional<StaffingRequest> findByRequestIdAndStatus(Long requestId, RequestStatus status);

    // Relationship filters
    List<StaffingRequest> findByProject_Id(Long projectId);

    List<StaffingRequest> findByDepartmentId(Long departmentId);

    // This now works because 'assignedUser' is defined in the Entity
    List<StaffingRequest> findByAssignedUser_Id(Long userId);

    List<StaffingRequest> findByStatus(RequestStatus status);


    /**
     * PostgreSQL Native Query for JSONB search.
     */
    @Query(
        value = """
            SELECT * FROM staffing_requests 
            WHERE required_skills @> CAST(:skills AS jsonb)
        """, 
        nativeQuery = true
    )
    List<StaffingRequest> findByRequiredSkills(@Param("skills") String skillsJson);


    @Query("""
    SELECT sr
    FROM StaffingRequest sr
    JOIN sr.department d
    WHERE sr.status = :status
      AND d.departmentHeadUserId = :departmentHeadUserId
    """)
    List<StaffingRequest> findPendingByDeptHead(
        @Param("status") RequestStatus status,
        @Param("departmentHeadUserId") Long departmentHeadUserId
    );
    // Navigation: Employee (createdBy) -> User (user) -> Email (email)
    List<StaffingRequest> findByCreatedBy_User_Email(String email);

    @Query("""
    SELECT sr
    FROM StaffingRequest sr
    JOIN sr.department d
    JOIN d.resourcePlanner u
    WHERE sr.status = :status
    AND u.email = :email
    """)
    List<StaffingRequest> findApprovedForResourcePlanner(
        @Param("status") RequestStatus status,
        @Param("email") String email
    );
    
}