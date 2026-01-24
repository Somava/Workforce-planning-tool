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
    FROM StaffingRequest sr, ProjectDepartment pd
    WHERE sr.status = :status
    AND pd.project.id = sr.project.id
    AND pd.department.id = sr.department.id
    AND pd.departmentHeadUser.id = :departmentHeadUserId
    """)
    List<StaffingRequest> findPendingByDeptHead(
        @Param("status") RequestStatus status,
        @Param("departmentHeadUserId") Long departmentHeadUserId
    );


    // Navigation: Employee (createdBy) -> User (user) -> Email (email)
    List<StaffingRequest> findByCreatedBy_User_Email(String email);

    @Query("""
    SELECT sr
    FROM StaffingRequest sr, ProjectDepartment pd
    WHERE sr.status = :status
    AND pd.project.id = sr.project.id
    AND pd.department.id = sr.department.id
    AND pd.resourcePlannerUser.email = :email
    """)
    List<StaffingRequest> findApprovedForResourcePlanner(
        @Param("status") RequestStatus status,
        @Param("email") String email
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
     * Dashboard Query: Fetches requests where the status is 'INT_EMPLOYEE_ASSIGNED'
     * and the logged-in user is either the Manager, Dept Head, Planner, or the Assigned Employee.
     */
    @Query("""
    SELECT DISTINCT s
    FROM StaffingRequest s
    LEFT JOIN s.assignedUser au
    LEFT JOIN s.createdBy cb
    LEFT JOIN cb.user cbu,
        ProjectDepartment pd
    LEFT JOIN pd.departmentHeadUser dh
    LEFT JOIN pd.resourcePlannerUser rp
    WHERE s.status = com.frauas.workforce_planning.model.enums.RequestStatus.INT_EMPLOYEE_ASSIGNED
    AND pd.project.id = s.project.id
    AND pd.department.id = s.department.id
    AND (
        cbu.email = :email
        OR dh.email = :email
        OR rp.email = :email
        OR au.email = :email
    )
    """)
    List<StaffingRequest> findSuccessDashboardData(@Param("email") String email);

}

    
