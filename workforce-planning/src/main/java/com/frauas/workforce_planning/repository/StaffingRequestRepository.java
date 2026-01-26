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

    List<StaffingRequest> findByCreatedByEmail(String email);

    // StaffingRequestRepository.java
    List<StaffingRequest> findByStatusIn(List<RequestStatus> status);
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
    WHERE sr.status = :status
    AND sr.department.id = :departmentId
    """)
    List<StaffingRequest> findPendingByDeptHead(
        @Param("status") RequestStatus status,
        @Param("departmentId") Long departmentId
    );


    // Navigation: Employee (createdBy) -> User (user) -> Email (email)
    List<StaffingRequest> findByCreatedBy_User_Email(String email);

    @Query("""
    SELECT sr
    FROM StaffingRequest sr
    WHERE sr.status = :status
    AND sr.department.id = :deptId
    """)
    List<StaffingRequest> findApprovedForResourcePlanner(
        @Param("status") RequestStatus status,
        @Param("deptId") Long deptId
    );

    @Query("""
    SELECT sr
    FROM StaffingRequest sr
    WHERE sr.status = :status
    AND sr.assignedUser.id = :userId
    """)
    List<StaffingRequest> findAssignedToEmployeeByStatus(
        @Param("status") RequestStatus status,
        @Param("userId") Long userId
    );

   /**
     * Dashboard Query: Fetches successful assignments for both Internal and External paths.
     * Triggers when:
     * 1. Internal Employee has confirmed (INT_EMPLOYEE_ASSIGNED)
     * 2. Dept Head has approved an External employee (EXT_EMPLOYEE_APPROVED_BY_DH)
     */
    @Query("""
    SELECT DISTINCT s
    FROM StaffingRequest s
    LEFT JOIN s.assignedUser au
    LEFT JOIN s.createdBy cb
    LEFT JOIN cb.user cbu
    JOIN ProjectDepartment pd ON pd.project.id = s.project.id AND pd.department.id = s.department.id
    LEFT JOIN pd.departmentHeadUser dh
    LEFT JOIN pd.resourcePlannerUser rp
    WHERE (
        s.status = com.frauas.workforce_planning.model.enums.RequestStatus.INT_EMPLOYEE_ASSIGNED
        OR 
        s.status = com.frauas.workforce_planning.model.enums.RequestStatus.EXT_EMPLOYEE_APPROVED_BY_DH
    )
    AND (
        cbu.email = :email
        OR dh.email = :email
        OR rp.email = :email
        OR (au IS NOT NULL AND au.email = :email)
    )
    """)
    List<StaffingRequest> findSuccessDashboardData(@Param("email") String email);

    @Query("""
        SELECT sr
        FROM StaffingRequest sr
        WHERE sr.status IN :statuses
        AND sr.department.id = :deptId
    """)
    List<StaffingRequest> findPendingApprovals(
        @Param("deptId") Long deptId, 
        @Param("statuses") List<RequestStatus> statuses
    );
}

    
