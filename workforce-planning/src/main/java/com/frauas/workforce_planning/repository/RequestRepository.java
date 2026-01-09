package com.frauas.workforce_planning.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.RequestEntity;
import com.frauas.workforce_planning.model.enums.RequestStatus;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    // 1. Find a single request by its custom ID
    Optional<RequestEntity> findByRequestId(Long requestId);

    // 2. Find all requests by a specific status
    List<RequestEntity> findAllByStatus(RequestStatus status);

    /**
     * 3. The "Magic" Derived Method
     * This performs an INNER JOIN: StaffingRequest -> Department
     * and filters by the User ID of the Department Head.
     */
    List<RequestEntity> findAllByStatusAndDepartment_DepartmentHeadUserId(
        RequestStatus status, 
        Long departmentHeadUserId
    );

    /**
     * 4. Explicit HQL Version (Recommended for debugging)
     * If the method above returns [], use this one to see if the mapping is correct.
     */
    @Query("SELECT r FROM RequestEntity r JOIN r.department d " +
           "WHERE r.status = :status AND d.departmentHeadUserId = :headId")
    List<RequestEntity> findPendingRequestsByDeptHead(
        @Param("status") RequestStatus status, 
        @Param("headId") Long headId
    );
}