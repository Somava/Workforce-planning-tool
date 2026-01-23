package com.frauas.workforce_planning.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.ExternalEmployee;

@Repository
public interface ExternalEmployeeRepository extends JpaRepository<ExternalEmployee, Long> {

    Optional<ExternalEmployee> findByExternalEmployeeId(String externalEmployeeId);

    List<ExternalEmployee> findByProvider(String provider);

    // This traverses ExternalEmployee -> StaffingRequest -> requestId
    List<ExternalEmployee> findByStaffingRequestId(Long staffingRequestId);

    @Query(value = "SELECT * FROM external_employees WHERE skills @> CAST(:skillsJson AS jsonb)", nativeQuery = true)
    List<ExternalEmployee> findBySkills(@Param("skillsJson") String skillsJson);

    // Find an employee by the ID provided by Team 3b
    Optional<ExternalEmployee> findByExternalId(String externalId);
}
